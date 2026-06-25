package com.cptrans.petrocarga.modules.reserva.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.exceptions.EmpresaExceptions;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.reserva.dto.mapper.ReservaMapper;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.reserva.exceptions.ReservaExceptions;
import com.cptrans.petrocarga.modules.reserva.repository.ReservaRepository;
import com.cptrans.petrocarga.modules.reservaRapida.dto.mapper.ReservaRapidaMapper;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.reservaRapida.repository.ReservaRapidaRepository;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.exceptions.VagaExceptions;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.shared.exceptions.DateExceptions;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservaUtils {
    private final EmpresaRepository empresaRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaRapidaRepository reservaRapidaRepository;

    public static final String METODO_POST = "POST";
    public static final String METODO_PATCH = "PATCH";
    public static final Integer LIMITE_DE_RESERVAS_POR_PLACA = 3;

    public static void validarTempoMaximoReserva(OffsetDateTime inicio, OffsetDateTime fim, AreaVagaEnum areaVaga, PermissaoEnum permissaoCriador) {
        validarHorarioReserva(inicio, fim);
        verificarExcecaoHorarioProAgente(permissaoCriador, inicio, fim);
        validarTempoMaximoReserva(inicio, fim, areaVaga);
    }

    public void validarEspacoDisponivelNaVaga(Reserva novaReserva, Usuario usuarioLogado, String metodoChamador) {
        Vaga vagaReserva = novaReserva.getVaga();
        Veiculo veiculoDaReserva = novaReserva.getVeiculo();
        Motorista motoristaDaReserva = novaReserva.getMotorista();
        Integer tamanhoDisponivelVaga = vagaReserva.getComprimento() - veiculoDaReserva.getTipo().getComprimento();
        ReservaDTO novaReservaDTO = ReservaMapper.toReservaDTO(novaReserva);
        List<ReservaDTO> reservasSobrepostas = getReservasAtivasSobrepostas(novaReserva.getInicio(), novaReserva.getFim());
        
        validarLimiteReservasPorPlaca(novaReservaDTO, reservasSobrepostas, metodoChamador);
        validarMotoristaReserva(motoristaDaReserva.getUsuario().getId(), motoristaDaReserva.getId(), reservasSobrepostas, metodoChamador);
        
        if (vagaReserva.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)){
            Integer posicaoPerpendicular = definirPosicaoVagaPerpendicular(vagaReserva.getTipoVaga(), vagaReserva.getQuantidade(), vagaReserva.getComprimento(), novaReservaDTO);
            novaReserva.setPosicaoPerpendicular(posicaoPerpendicular);
            novaReservaDTO.setPosicaoPerpendicular(posicaoPerpendicular);
            validarPosicaoPerpendicular(vagaReserva.getTipoVaga(), vagaReserva.getQuantidade(), novaReservaDTO.getPosicaoPerpendicular());
            validarCapacidadePerpendicularPorPosicao(vagaReserva.getTipoVaga(), vagaReserva.getComprimento(), novaReservaDTO, reservasSobrepostas, novaReserva.getPosicaoPerpendicular());
            
            return;
        }

        if (!reservasSobrepostas.isEmpty()){
            for (ReservaDTO reserva : reservasSobrepostas){ 
                if (metodoChamador.equals(METODO_PATCH) && motoristaDaReserva.getUsuario().getId().equals(usuarioLogado.getId())){
                    if (tamanhoDisponivelVaga < 0) throw new ReservaExceptions.EspacoInsuficienteNoPeriodoException();
                    return;
                }  
                tamanhoDisponivelVaga -= reserva.getTamanhoVeiculo();
                if (tamanhoDisponivelVaga < 0) throw new ReservaExceptions.EspacoInsuficienteNoPeriodoException();
            }
        }
    }

    public void validarLimiteReservasPorPlaca (ReservaDTO novaReserva, List<ReservaDTO> reservasSobrepostas, String metodoChamador){
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        Integer quantidadeReservasPorPlaca = reservaRepository.countByVeiculoPlacaIgnoringCaseAndStatusIn(novaReserva.getPlacaVeiculo(), listaStatus);
        if (quantidadeReservasPorPlaca >= (LIMITE_DE_RESERVAS_POR_PLACA)) throw new ReservaExceptions.LimiteDeReservasPorPlacaException(LIMITE_DE_RESERVAS_POR_PLACA);
        if (reservasSobrepostas != null && !reservasSobrepostas.isEmpty()  ){
            for (ReservaDTO reserva : reservasSobrepostas){
                if (reserva.getPlacaVeiculo().equals(novaReserva.getPlacaVeiculo()) && metodoChamador.equals(METODO_POST)) {
                    throw new ReservaExceptions.PlacaComConflitoDeHorarioException();
                }
            } 
        }
    }

    public void validarPermissoesReserva(UUID usuarioLogadoId, PermissaoEnum permissaoUsuarioLogado, Motorista motoristaDaReserva, Veiculo veiculoDaReserva) {
        if (permissaoUsuarioLogado.equals(PermissaoEnum.MOTORISTA)){
            if (!veiculoDaReserva.getUsuario().getId().equals(usuarioLogadoId)){
                if (motoristaDaReserva.getEmpresa() != null && motoristaDaReserva.getVeiculosEmpresa() != null && !motoristaDaReserva.getVeiculosEmpresa().isEmpty()){
                    List<Veiculo> veiculosEmpresa = motoristaDaReserva.getVeiculosEmpresa().stream().map(veiculoEmpresaMotorista -> veiculoEmpresaMotorista.getVeiculo()).toList();
                    if (veiculosEmpresa.contains(veiculoDaReserva) && motoristaDaReserva.getEmpresa().getUsuario().getId().equals(veiculoDaReserva.getUsuario().getId())){
                        return;
                    }
                }   
                throw new ReservaExceptions.VeiculoNaoPertenceException();
            }

            if (!motoristaDaReserva.getUsuario().getId().equals(usuarioLogadoId)){
                throw new ReservaExceptions.MotoristaInvalidoException();
            }
        }
        
        if (permissaoUsuarioLogado.equals(PermissaoEnum.EMPRESA)){
            if (!veiculoDaReserva.getUsuario().getId().equals(usuarioLogadoId)){
                throw new ReservaExceptions.VeiculoNaoPertenceException();
            }
            Empresa empresa = empresaRepository.findByUsuarioId(usuarioLogadoId).orElseThrow(() -> new EmpresaExceptions.EmpresaNotFoundException());
            if (motoristaDaReserva.getEmpresa() == null || !motoristaDaReserva.getEmpresa().getId().equals(empresa.getId())){
                throw new ReservaExceptions.MotoristaNaoPertenceEmpresaException();
            }
        }
    }

    public void validarMotoristaReserva(UUID motoristaUsuarioId, UUID motoristaId, List<ReservaDTO> reservasSobrepostas, String metodoChamador) {
        List<ReservaDTO> reservasAtivasSobrepostasPorMotorista = reservasSobrepostas.stream().filter(reserva -> reserva.getCriadoPor().getId().equals(motoristaUsuarioId) || reserva.getMotoristaId().equals(motoristaId)).toList();
        if(reservasAtivasSobrepostasPorMotorista != null && !reservasAtivasSobrepostasPorMotorista.isEmpty() && metodoChamador.equals(METODO_POST)) {
            throw new ReservaExceptions.MotoristaComConflitoDeHorarioException();
        }
    }

    public static List<ReservaDTO> juntarReservas(List<Reserva> reservas, List<ReservaRapida> reservasRapidas) {
        List<ReservaDTO> listaFinalReservas = new ArrayList<>(); 

        if (reservasRapidas != null && !reservasRapidas.isEmpty()) {
            reservasRapidas.forEach((rr) -> {
                listaFinalReservas.add(ReservaRapidaMapper.toReservaDTO(rr));
            });
        }
    
        if (reservas != null && !reservas.isEmpty()) {
            reservas.forEach((r) -> {
                listaFinalReservas.add(ReservaMapper.toReservaDTO(r));
            });
        }
    
        return listaFinalReservas;
    }

    public Boolean existsByUsuarioId(UUID usuarioId) {
        return reservaRepository.existsByCriadoPorIdOrMotoristaUsuarioId(usuarioId);
    }

    public static void validarFiltrosData(LocalDate data, Integer mes, Integer ano) {
        boolean informouData = data != null;
        boolean informouMesEAno = mes != null && ano != null;

        if ((informouData && informouMesEAno)) {
            throw new DateExceptions.FiltroDataInvalidoException();
        }

        DateUtils.validarMesEAno(mes, ano);
    }

    public static void validarCapacidadePerpendicularPorPosicao(
        TipoVagaEnum tipoVaga,
        Integer comprimentoVaga,
        ReservaDTO novaReserva,
        List<ReservaDTO> reservasSobrepostasNaVaga,
        Integer posicaoPerpendicular
    ) {
        if (!TipoVagaEnum.PERPENDICULAR.equals(tipoVaga)) {
            return;
        }

        if (comprimentoVaga == null || comprimentoVaga <= 0) {
            throw new VagaExceptions.ComprimentoInvalidoException();
        }
        if (novaReserva.getTamanhoVeiculo() > comprimentoVaga) {
            throw new ReservaExceptions.VeiculoMaiorQueVagaException();
        }

        int ocupacaoAtual = reservasSobrepostasNaVaga != null && !reservasSobrepostasNaVaga.isEmpty() ?
                reservasSobrepostasNaVaga.stream()
                .filter(reserva -> posicaoPerpendicular.equals(reserva.getPosicaoPerpendicular()))
                .mapToInt(ReservaDTO::getTamanhoVeiculo)
                .sum() : 0;

        int ocupacaoFinal = ocupacaoAtual + novaReserva.getTamanhoVeiculo();

        if (ocupacaoFinal > comprimentoVaga) {
            throw new ReservaExceptions.EspacoInsuficienteNoPeriodoException();
        }
    }

    public static void validarPosicaoPerpendicular(TipoVagaEnum tipoVaga, Integer quantidadePosicoesVaga, Integer posicaoPerpendicular) {
        if (!TipoVagaEnum.PERPENDICULAR.equals(tipoVaga)) {
            if (posicaoPerpendicular != null) {
                throw new ReservaExceptions.PosicaoPerpendicularProibidaException();
            }
            return;
        }

        if (quantidadePosicoesVaga == null || quantidadePosicoesVaga <= 0) {
            throw new VagaExceptions.QuantidadePosicoesInvalidaException();
        }

        if (posicaoPerpendicular == null || posicaoPerpendicular <= 0 || posicaoPerpendicular > quantidadePosicoesVaga) {
            throw new ReservaExceptions.PosicaoPerpendicularInvalidaException(quantidadePosicoesVaga);
        }

    }

    public static Integer encontrarPosicaoDisponivel(
        Integer quantidadePosicoesVaga,
        Integer comprimentoVaga,
        ReservaDTO novaReserva,
        List<ReservaDTO> reservasSobrepostasNaVaga
    ) {
        if (quantidadePosicoesVaga == null || quantidadePosicoesVaga <= 0) {
            throw new VagaExceptions.QuantidadePosicoesInvalidaException();
        }
        for (int posicao = 1; posicao <= quantidadePosicoesVaga; posicao++) {
            final int posicaoAtual = posicao;
            int ocupado = reservasSobrepostasNaVaga != null && !reservasSobrepostasNaVaga.isEmpty() ?
                reservasSobrepostasNaVaga.stream()
                    .filter(reserva -> posicaoAtual == reserva.getPosicaoPerpendicular())
                    .mapToInt(ReservaDTO::getTamanhoVeiculo)
                    .sum() : 0;

            if (ocupado + novaReserva.getTamanhoVeiculo() <= comprimentoVaga) {
                return posicaoAtual;
            }
        }

        throw new ReservaExceptions.TodasPosicoesOcupadasException();
    }

    public static void validarHorarioReserva(OffsetDateTime inicio, OffsetDateTime fim) {
        if (inicio == null || fim == null) throw new ReservaExceptions.InicioEFimObrigatoriosException();
        if (!inicio.toInstant().isBefore(fim.toInstant())) throw new ReservaExceptions.InicioEFimInvalidosException();
    }

    private static void validarTempoMaximoReserva(OffsetDateTime inicio, OffsetDateTime fim, AreaVagaEnum areaVaga) {
        Integer tempoReservaEmMinutos = (int) (inicio.toInstant().until(fim.toInstant(), ChronoUnit.MINUTES));
        Boolean tempoValido = ((tempoReservaEmMinutos <= (areaVaga.getTempoMaximo() * 60)) && (tempoReservaEmMinutos > 0));
        if (!tempoValido){
            throw new VagaExceptions.TempoPermanenciaInvalidoExcpetion(areaVaga.getTempoMaximo());
        }
    }


    private static void verificarExcecaoHorarioProAgente(PermissaoEnum permissaoCriador, OffsetDateTime inicio, OffsetDateTime fim){
        OffsetDateTime agora = DateUtils.agora();
        LocalDate dataAgora = agora.toLocalDate();
        LocalDate dataInicio =  inicio.toLocalDate();
        LocalDate dataFim = fim.toLocalDate();
        if (dataAgora.equals(dataInicio) && dataAgora.equals(dataFim)) {
            long diferencaMinutos = Duration.between(agora, inicio).toMinutes();
            if (diferencaMinutos >= 0) return;
            else if (permissaoCriador.equals(PermissaoEnum.AGENTE) && diferencaMinutos >= -2) return;
            else throw new ReservaExceptions.InicioEFimInvalidosException();
        } else {
            throw new ReservaExceptions.InicioEFimInvalidosException();
        }
    }

    public List<ReservaDTO> getReservasAtivasSobrepostas(OffsetDateTime inicio, OffsetDateTime fim) {
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        List<Reserva> reservasNormaisSobrepostas = reservaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(inicio, fim, listaStatus);
        List<ReservaRapida> reservasRapidasSobrepostas = reservaRapidaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(inicio, fim, listaStatus);
        return juntarReservas(reservasNormaisSobrepostas, reservasRapidasSobrepostas);
    }

    public Integer definirPosicaoVagaPerpendicular(TipoVagaEnum tipoVaga, Integer quantidadePosicoesVaga, Integer comprimentoVaga, ReservaDTO novaReserva) {
        if (tipoVaga != null && tipoVaga.equals(TipoVagaEnum.PERPENDICULAR)) {
            if (novaReserva.getPosicaoPerpendicular() == null || novaReserva.getPosicaoPerpendicular() <= 0 || novaReserva.getPosicaoPerpendicular() > quantidadePosicoesVaga) {
                return encontrarPosicaoDisponivel(quantidadePosicoesVaga, comprimentoVaga, novaReserva, getReservasAtivasSobrepostas(novaReserva.getInicio(), novaReserva.getFim()));
            }
        }
        return novaReserva.getPosicaoPerpendicular();
    }
}