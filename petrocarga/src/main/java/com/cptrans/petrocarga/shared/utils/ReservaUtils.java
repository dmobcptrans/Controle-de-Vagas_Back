package com.cptrans.petrocarga.shared.utils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.application.dto.ReservaDTO;
import com.cptrans.petrocarga.application.usecase.EmpresaService;
import com.cptrans.petrocarga.domain.entities.Empresa;
import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.entities.Veiculo;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVagaEnum;
import com.cptrans.petrocarga.domain.repositories.ReservaRapidaRepository;
import com.cptrans.petrocarga.domain.repositories.ReservaRepository;

@Component
public class ReservaUtils {
    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private ReservaRapidaRepository reservaRapidaRepository;

    public static final String METODO_POST = "POST";
    public static final String METODO_PATCH = "PATCH";
    public static final Integer LIMITE_DE_RESERVAS_POR_PLACA = 3;

    public static void validarTempoMaximoReserva(ReservaDTO novaReserva, Vaga vagaNovaReserva) {
        OffsetDateTime agora = OffsetDateTime.now(DateUtils.FUSO_BRASIL);
        if(novaReserva.getFim().toInstant().isBefore(novaReserva.getInicio().toInstant()) || novaReserva.getFim().toInstant().isBefore(agora.toInstant()) ) {
            throw new IllegalArgumentException("Horário de Fim da reserva deve ser posterior ao horário de início e do horário atual.");
        }

        if(novaReserva.getInicio().toInstant().isBefore(agora.toInstant())) {
            LocalDate dataAgora = agora.toLocalDate();
            LocalDate dataInicio =  novaReserva.getInicio().toLocalDate();
            LocalDate dataFim = novaReserva.getFim().toLocalDate();
            if(novaReserva.getCriadoPor().getPermissao().equals(PermissaoEnum.AGENTE)){
                if (dataAgora.equals(dataInicio) && dataAgora.equals(dataFim) && (agora.getHour() == novaReserva.getInicio().getHour())) {
                    Integer diferencaMinutos = agora.getMinute() - novaReserva.getInicio().getMinute();
                    if (diferencaMinutos >= 2 || diferencaMinutos < 0){
                        throw new IllegalArgumentException("Horário da reserva deve ser posterior ao horário atual.");
                    }else{
                        return;
                    }
                }
            }
            throw new IllegalArgumentException("Horário da reserva deve ser posterior ao horário atual.");
        }

        Integer tempoReservaEmMinutos = (int) (novaReserva.getInicio().toInstant().until(novaReserva.getFim().toInstant(), java.time.temporal.ChronoUnit.MINUTES));
        Boolean tempoValido = tempoReservaEmMinutos <= (vagaNovaReserva.getArea().getTempoMaximo() * 60) && tempoReservaEmMinutos > 0;
        if(!tempoValido){
            throw new IllegalArgumentException("Tempo total de reserva inválido.");
        }
    }

    public void validarEspacoDisponivelNaVaga(Reserva novaReserva, Usuario usuarioLogado, List<Reserva> reservasAtivasNaVaga, List<ReservaRapida> reservasRapidasAtivasNaVaga, String metodoChamador) {
        Vaga vagaReserva = novaReserva.getVaga();
        Veiculo veiculoDaReserva = novaReserva.getVeiculo();
        Motorista motoristaDaReserva = novaReserva.getMotorista();
        Integer tamanhoDisponivelVaga = vagaReserva.getComprimento() - veiculoDaReserva.getTipo().getComprimento();
        List<ReservaDTO> reservasVaga = juntarReservas(reservasAtivasNaVaga, reservasRapidasAtivasNaVaga);
        ReservaDTO novaReservaDTO = novaReserva.toReservaDTO();
        
        validarLimiteReservasPorPlaca(novaReservaDTO, metodoChamador);
        validarMotoristaReserva(motoristaDaReserva.getUsuario().getId(), novaReservaDTO, metodoChamador);
        
        if(vagaReserva.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)){
            validarPosicaoPerpendicular(vagaReserva.getTipoVaga(), vagaReserva.getQuantidade(), novaReserva.getPosicaoPerpendicular());
            validarCapacidadePerpendicularPorPosicao(vagaReserva, novaReservaDTO, reservasVaga, novaReserva.getPosicaoPerpendicular());
            return;
        }

        if(!reservasVaga.isEmpty()){
            for(ReservaDTO reserva : reservasVaga){ 
                Boolean reservaSobrepostas = novaReserva.getInicio().toInstant().isBefore(reserva.getFim().toInstant()) && novaReserva.getFim().toInstant().isAfter(reserva.getInicio().toInstant());
                if(reservaSobrepostas){
                    if(metodoChamador.equals(METODO_PATCH) && motoristaDaReserva.getUsuario().getId().equals(usuarioLogado.getId())){
                    if(tamanhoDisponivelVaga < 0) throw new IllegalArgumentException("Não há espaço suficiente na vaga para a reserva no período solicitado devido a uma reserva existente. Espaço disponível: " + (tamanhoDisponivelVaga + veiculoDaReserva.getTipo().getComprimento()) + " metros.");
                        return;
                    }  
                    tamanhoDisponivelVaga -= reserva.getTamanhoVeiculo();
                    if(tamanhoDisponivelVaga < 0) throw new IllegalArgumentException("Não há espaço suficiente na vaga para a reserva no período solicitado devido a uma reserva existente. Espaço disponível: " + (tamanhoDisponivelVaga + veiculoDaReserva.getTipo().getComprimento()) + " metros.");
                }
            }
        }
    }

    public void validarLimiteReservasPorPlaca (ReservaDTO novaReserva, String metodoChamador){
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        Integer quantidadeReservasPorPlaca = reservaRepository.countByVeiculoPlacaIgnoringCaseAndStatusIn(novaReserva.getPlacaVeiculo(), listaStatus);
        List<Reserva> reservasNormaisSobrepostas = reservaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(novaReserva.getInicio(), novaReserva.getFim(), listaStatus);
        List<ReservaRapida> reservasRapidasSobrepostas = reservaRapidaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(novaReserva.getInicio(), novaReserva.getFim(), listaStatus);
        List<ReservaDTO> reservasSobrepostas = juntarReservas(reservasNormaisSobrepostas, reservasRapidasSobrepostas);
        if (quantidadeReservasPorPlaca.equals(LIMITE_DE_RESERVAS_POR_PLACA)) throw new IllegalArgumentException("Veículo de placa " + novaReserva.getPlacaVeiculo() + " ja atingiu o limite de " + LIMITE_DE_RESERVAS_POR_PLACA + " reservas ativas/reservadas.");
        if(reservasSobrepostas != null && !reservasSobrepostas.isEmpty()  ){
            for(ReservaDTO reserva : reservasSobrepostas){
                if(reserva.getPlacaVeiculo().equals(novaReserva.getPlacaVeiculo()) && !metodoChamador.equals(METODO_PATCH)) {
                    throw new IllegalArgumentException("Veículo de placa " + novaReserva.getPlacaVeiculo() + " ja possui uma reserva com status: " + reserva.getStatus() + " com inicio: " + reserva.getInicio().atZoneSameInstant(DateUtils.FUSO_BRASIL) + " e fim: " + reserva.getFim().atZoneSameInstant(DateUtils.FUSO_BRASIL) + ".");
                }
            } 
        }
    }

    public void validarPermissoesReserva(Usuario usuarioLogado, Motorista motoristaDaReserva, Veiculo veiculoDaReserva) {
        if (usuarioLogado.getPermissao().equals(PermissaoEnum.MOTORISTA) || usuarioLogado.getPermissao().equals(PermissaoEnum.EMPRESA)){
            if(!veiculoDaReserva.getUsuario().getId().equals(usuarioLogado.getId())){
                throw new IllegalArgumentException("Usuário não pode fazer reserva para um veículo de outro usuário.");
            }
        }

        if (usuarioLogado.getPermissao().equals(PermissaoEnum.MOTORISTA)){
            if(!motoristaDaReserva.getUsuario().getId().equals(usuarioLogado.getId())){
                throw new IllegalArgumentException("Usuário não pode fazer reserva para outro motorista.");
            }
        }
        
        if (usuarioLogado.getPermissao().equals(PermissaoEnum.EMPRESA)){
            Empresa empresa = empresaService.findByUsuarioId(usuarioLogado.getId());
            if(!motoristaDaReserva.getEmpresa().getId().equals(empresa.getId())){
                throw new IllegalArgumentException("A empresa só pode fazer reserva para motoristas associados à ela.");
            }
        }
    }

    public void validarMotoristaReserva( UUID motoristaUsuarioId, ReservaDTO novaReserva, String metodoChamador) {
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        List<Reserva> reservasAtivasSobrepostasPorMotorista = reservaRepository.findByFimGreaterThanAndInicioLessThanAndMotoristaUsuarioIdAndStatusIn(novaReserva.getInicio(),novaReserva.getFim(), motoristaUsuarioId, listaStatus);
        if(reservasAtivasSobrepostasPorMotorista != null && !reservasAtivasSobrepostasPorMotorista.isEmpty() && !metodoChamador.equals(METODO_PATCH)){
            throw new IllegalArgumentException("Motorista já possui uma reserva ativa ou reservada com horário conflitante.");
        }
    }

    public static List<ReservaDTO> juntarReservas(List<Reserva> reservas, List<ReservaRapida> reservasRapidas) {
        List<ReservaDTO> listaFinalReservas = new ArrayList<>(); 

        if(reservasRapidas != null && !reservasRapidas.isEmpty()) {
                reservasRapidas.forEach(rr -> listaFinalReservas.add(new ReservaDTO(rr.getId(), rr.getVaga().getId(), rr.getVaga().getNumeroEndereco(), rr.getVaga().getReferenciaEndereco(), rr.getVaga().getEndereco().toResponseDTO(), rr.getInicio(), rr.getFim(), rr.getTipoVeiculo().getComprimento(), rr.getPlaca(), rr.getStatus(), rr.getAgente().getUsuario(), rr.getCriadoEm(), rr.getPosicaoPerpendicular(), rr.getCidadeOrigem(), rr.getEntradaCidade())));
        }
    
        if(reservas != null && !reservas.isEmpty()) {
            reservas.forEach(r-> listaFinalReservas.add(new ReservaDTO(r.getId(), r.getCidadeOrigem(), r.getEntradaCidade(), r.isCheckedIn(), r.getCheckInEm(), r.getCheckOutEm(), r.getVaga(), r.getInicio(), r.getFim(), r.getVeiculo(), r.getStatus(), r.getCriadoPor(), r.getCriadoEm(), r.getMotorista(), r.getPosicaoPerpendicular())));
        }
    
        return listaFinalReservas;
    }

    public Boolean existsByUsuarioId(UUID usuarioId) {
        return reservaRepository.existsByCriadoPorIdOrMotoristaUsuarioId(usuarioId);
    }

    public static void validarFiltrosData(LocalDate data, Integer mes, Integer ano) {
        boolean informouData = data != null;
        boolean informouMesOuAno = mes != null || ano != null;

        if (informouData && informouMesOuAno) {
            throw new IllegalArgumentException("Informe ou a data completa ou mês e ano.");
        }

        if (mes != null && ano == null) {
            throw new IllegalArgumentException("Ao informar o mês, o ano também deve ser informado.");
        }

        if (ano != null && mes == null) {
            throw new IllegalArgumentException("Ao informar o ano, o mês também deve ser informado.");
        }

        DateUtils.validarMesEAno(mes, ano);
    }

    public static void validarCapacidadePerpendicularPorPosicao(
        Vaga vaga,
        ReservaDTO novaReserva,
        List<ReservaDTO> reservasAtivasNaVaga,
        Integer posicaoPerpendicular
    ) {
        if (!TipoVagaEnum.PERPENDICULAR.equals(vaga.getTipoVaga())) {
            throw new IllegalArgumentException("Validação de posição perpendicular só pode ser aplicada para vagas perpendiculares.");
        }

        if (vaga.getComprimento() == null || vaga.getComprimento() <= 0) {
            throw new IllegalArgumentException("Vaga perpendicular deve ter comprimento válido por posição.");
        }

        if (novaReserva.getTamanhoVeiculo() > vaga.getComprimento()) {
            throw new IllegalArgumentException("O veículo é maior do que o tamanho permitido para esta posição.");
        }

        int ocupacaoAtual = reservasAtivasNaVaga.stream()
            .filter(reserva -> posicaoPerpendicular.equals(reserva.getPosicaoPerpendicular()))
            .filter(reserva ->
                reserva.getInicio().toInstant().isBefore(novaReserva.getFim().toInstant()) &&
                reserva.getFim().toInstant().isAfter(novaReserva.getInicio().toInstant())
            )
            .mapToInt(ReservaDTO::getTamanhoVeiculo)
            .sum();

        int ocupacaoFinal = ocupacaoAtual + novaReserva.getTamanhoVeiculo();

        if (ocupacaoFinal > vaga.getComprimento()) {
            throw new IllegalArgumentException(
                "Não há espaço suficiente na posição " + posicaoPerpendicular +
                " para o período solicitado. Capacidade da posição: " + vaga.getComprimento() +
                " metros. Ocupação já existente: " + ocupacaoAtual + " metros."
            );
        }
    }

    public static void validarPosicaoPerpendicular(TipoVagaEnum tipoVaga, Integer quantidadePosicoesVaga, Integer posicaoPerpendicular) {
        if (!TipoVagaEnum.PERPENDICULAR.equals(tipoVaga)) {
            if (posicaoPerpendicular != null) {
                throw new IllegalArgumentException("Posição só pode ser informada para vaga do tipo perpendicular.");
            }
            return;
        }

        if (quantidadePosicoesVaga == null || quantidadePosicoesVaga <= 0) {
            throw new IllegalArgumentException("Vaga perpendicular deve ter quantidade de posições válida.");
        }

        if (posicaoPerpendicular == null) {
            throw new IllegalArgumentException("Para vaga perpendicular, a posição é obrigatória.");
        }

        if (posicaoPerpendicular < 1 || posicaoPerpendicular > quantidadePosicoesVaga) {
            throw new IllegalArgumentException("Posição inválida para esta vaga. Informe um valor entre 1 e " + quantidadePosicoesVaga + ".");
        }
    }

    public static Integer encontrarPosicaoDisponivel(
        Integer quantidadePosicoesVaga,
        Integer comprimentoVaga,
        ReservaDTO novaReserva,
        List<ReservaDTO> reservasAtivas
    ) {
        if(quantidadePosicoesVaga == null || quantidadePosicoesVaga <= 0) {
            throw new IllegalArgumentException("Quantidade de posições da vaga deve ser informada e maior que zero.");
        }
        for (int posicao = 1; posicao <= quantidadePosicoesVaga; posicao++) {
            final int posicaoAtual = posicao;
            int ocupado = reservasAtivas.stream()
                .filter(reserva -> posicaoAtual == reserva.getPosicaoPerpendicular())
                .filter(reserva ->
                    reserva.getInicio().toInstant().isBefore(novaReserva.getFim().toInstant()) &&
                    reserva.getFim().toInstant().isAfter(novaReserva.getInicio().toInstant())
                )
                .mapToInt(ReservaDTO::getTamanhoVeiculo)
                .sum();

            if (ocupado + novaReserva.getTamanhoVeiculo() <= comprimentoVaga) {
                return posicaoAtual;
            }
        }

        throw new IllegalArgumentException("Não há posições disponíveis nesta vaga no período solicitado.");
    }
}