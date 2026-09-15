package com.cptrans.petrocarga.modules.reservaRules;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.auth.utils.AuthUtils;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.service.DisponibilidadeVagaService;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.operacaoVaga.utils.OperacaoVagaUtils;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.exceptions.ReservaExceptions;
import com.cptrans.petrocarga.modules.reserva.repository.ReservaRepository;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.reservaRapida.repository.ReservaRapidaRepository;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.exceptions.VagaExceptions;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculo.exceptions.VeiculoExceptions;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class ReservaRulesImpl implements ReservaRules {
    private final DisponibilidadeVagaService disponibilidadeVagaService;
    private final ReservaRepository reservaRepository;
    private final ReservaRapidaRepository reservaRapidaRepository;
    private final ReservaUtils reservaUtils;

    private final int LIMITE_DE_RESERVAS_ATIVAS_POR_PLACA = 3;
    private final int LIMITE_DE_RESERVAS_ATIVAS_POR_VEICULO = 3;
    private final int LIMITE_DE_RESERVAS_ATIVAS_POR_MOTORISTA = 3;
    private final int LIMITE_DE_RESERVAS_RAPIDAS_POR_PLACA = 3;

    
    @Override
    public void validacoesDaVaga(Vaga vaga, OffsetDateTime inicio, OffsetDateTime fim) {
        validarTempoMaximo(inicio, fim, vaga.getArea());
        if (!disponibilidadeVagaService.existsByVagaIdAndInicioAndFim(vaga.getId(), inicio, fim)) throw new ReservaExceptions.VagaIndisponivelException();
        OperacaoVagaUtils.verificarLimiteHorarioOperacaoVaga(vaga.getOperacoesVaga(), inicio, fim);
        
    }

    @Override
    public void validacoesDeAutorizacao(UUID usuarioLogadoId, Motorista motorista, Veiculo veiculo) {
        //Verifica se o usuário logado é uma empresa, 
        // se o motorista pertence à ela 
        // e se o motorista está autorizado à utilizar o veículo
        if (AuthUtils.containsAuthority(List.of(PermissaoEnum.EMPRESA.getRole()))) {
            if (
                motorista.getEmpresa() == null || 
                (
                    motorista.getEmpresa() != null && 
                    !motorista.getEmpresa().getId().equals(usuarioLogadoId)
                )
            ){ 
                throw new ReservaExceptions.MotoristaNaoPertenceEmpresaException();
            }
            if (!veiculo.getMotoristasIds().stream().anyMatch((id) -> id.equals(motorista.getId()))){
                throw new VeiculoExceptions.MotoristaNaoVinculadoAoVeiculoException();
            }
        }

        //Verifica se o usuário logado NÂO é GESTOR ou ADMIN
        //Verfica se o veiculo NÂO pertence ao usuário logado 
        //Verifica se o veiculo NÂO pertence ao motorista
        UUID donoVeiculoId = veiculo.getUsuario().getId();
        if (
            !AuthUtils.containsAuthority(List.of(PermissaoEnum.ADMIN.getRole(), PermissaoEnum.GESTOR.getRole())) &&
            !donoVeiculoId.equals(usuarioLogadoId) && 
            !donoVeiculoId.equals(motorista.getId())
        ){
            throw new AuthExceptions.UsuarioNaoAutorizadoException();
        }
    }

    @Override
    public void validacoesDeConflito(Vaga vaga, ReservaDTO reserva) {
        validarLimiteDeReservasAtivas(reserva);
        validarConflitoDePlacaVeiculoOuMotorista(reserva);
        validarEspacoDisponivel(vaga, reserva);
    }

    @Override 
    public void validacoesTemporais(OffsetDateTime inicio, OffsetDateTime fim) {
        validarInicioEFim(inicio, fim);
        verificarExcecaoHorarioInicio(inicio, fim);
    }

    @Override 
    public void validarLimiteIntrodutorioReservasRapidas(String placa) {
        placa = placa.trim().toUpperCase();
        Integer quantidadeReservasRapidasPorPlaca = reservaRapidaRepository.countByPlacaIgnoringCase(placa);
        if (quantidadeReservasRapidasPorPlaca >= LIMITE_DE_RESERVAS_RAPIDAS_POR_PLACA){
            throw new ReservaExceptions.LimiteDeReservasPorPlacaException(LIMITE_DE_RESERVAS_RAPIDAS_POR_PLACA);
        }
    }

    private void validarInicioEFim(OffsetDateTime inicio, OffsetDateTime fim) {
        if (inicio == null || fim == null) throw new ReservaExceptions.InicioEFimObrigatoriosException();
        if (!inicio.toInstant().isBefore(fim.toInstant())) throw new ReservaExceptions.InicioEFimInvalidosException();
    }

    private void validarTempoMaximo(OffsetDateTime inicio, OffsetDateTime fim, AreaVagaEnum areaVaga) {
        Integer tempoReservaEmMinutos = (int) (inicio.toInstant().until(fim.toInstant(), ChronoUnit.MINUTES));
        Boolean tempoValido = ((tempoReservaEmMinutos <= (areaVaga.getTempoMaximo() * 60)) && (tempoReservaEmMinutos > 0));
        if (!tempoValido){
            throw new VagaExceptions.TempoPermanenciaInvalidoExcpetion(areaVaga.getTempoMaximo());
        }
    }

    private void verificarExcecaoHorarioInicio(OffsetDateTime inicio, OffsetDateTime fim){
        OffsetDateTime agora = DateUtils.agora();
        LocalDate dataAgora = agora.toLocalDate();
        LocalDate dataInicio =  inicio.toLocalDate();
        if (dataAgora.equals(dataInicio)) {
            long diferencaMinutos = Duration.between(agora, inicio).toMinutes();
            if (diferencaMinutos >= -2) return;
            else throw new ReservaExceptions.InicioEFimInvalidosException();
        }
    }
    
    private void validarLimiteDeReservasAtivas (ReservaDTO reserva){
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        String placa = reserva.getPlacaVeiculo().trim().toUpperCase();
        
        if (reserva.getId() != null){
            Integer quantidadeReservasPorPlaca = reservaRepository.countByVeiculoPlacaIgnoringCaseAndStatusInAndIdNot(placa, listaStatus, reserva.getId());
            Integer quantidadeReservasRapidasPorPlaca = reservaRapidaRepository.countByPlacaIgnoringCaseAndStatusInAndIdNot(placa, listaStatus, reserva.getId());
            Integer quantidadeReservasVeiculoId = reserva.getVeiculoId() != null ? reservaRepository.countByVeiculoIdAndStatusInAndIdNot(reserva.getVeiculoId(), listaStatus, reserva.getId()) : 0;
            Integer quantidadeReservasMotoristaId = reserva.getMotoristaId() != null ? reservaRepository.countByMotoristaIdAndStatusInAndIdNot(reserva.getMotoristaId(), listaStatus, reserva.getId()) : 0;
        
            if (quantidadeReservasPorPlaca + quantidadeReservasRapidasPorPlaca >= (LIMITE_DE_RESERVAS_ATIVAS_POR_PLACA) ) throw new ReservaExceptions.LimiteDeReservasPorPlacaException(LIMITE_DE_RESERVAS_ATIVAS_POR_PLACA);
            if (quantidadeReservasVeiculoId >= (LIMITE_DE_RESERVAS_ATIVAS_POR_VEICULO) ) throw new ReservaExceptions.LimiteDeReservasPorVeiculoException(LIMITE_DE_RESERVAS_ATIVAS_POR_VEICULO);
            if (quantidadeReservasMotoristaId >= (LIMITE_DE_RESERVAS_ATIVAS_POR_MOTORISTA) ) throw new ReservaExceptions.LimiteDeReservasPorMotoristaException(LIMITE_DE_RESERVAS_ATIVAS_POR_MOTORISTA);

        } else {
            Integer quantidadeReservasPorPlaca = reservaRepository.countByVeiculoPlacaIgnoringCaseAndStatusIn(placa, listaStatus);
            Integer quantidadeReservasRapidasPorPlaca = reservaRapidaRepository.countByPlacaIgnoringCaseAndStatusIn(placa, listaStatus);
            Integer quantidadeReservasVeiculoId = reserva.getVeiculoId() != null ? reservaRepository.countByVeiculoIdAndStatusIn(reserva.getVeiculoId(), listaStatus) : 0;
            Integer quantidadeReservasMotoristaId = reserva.getMotoristaId() != null ? reservaRepository.countByMotoristaIdAndStatusIn(reserva.getMotoristaId(), listaStatus) : 0;

            if (quantidadeReservasPorPlaca + quantidadeReservasRapidasPorPlaca >= (LIMITE_DE_RESERVAS_ATIVAS_POR_PLACA) ) throw new ReservaExceptions.LimiteDeReservasPorPlacaException(LIMITE_DE_RESERVAS_ATIVAS_POR_PLACA);
            if (quantidadeReservasVeiculoId >= (LIMITE_DE_RESERVAS_ATIVAS_POR_VEICULO) ) throw new ReservaExceptions.LimiteDeReservasPorVeiculoException(LIMITE_DE_RESERVAS_ATIVAS_POR_VEICULO);
            if (quantidadeReservasMotoristaId >= (LIMITE_DE_RESERVAS_ATIVAS_POR_MOTORISTA) ) throw new ReservaExceptions.LimiteDeReservasPorMotoristaException(LIMITE_DE_RESERVAS_ATIVAS_POR_MOTORISTA);
        }

    }

    private void validarConflitoDePlacaVeiculoOuMotorista(ReservaDTO reserva){
        String placa = reserva.getPlacaVeiculo().trim().toUpperCase();
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        
        //verifica se a reserva já existe
        if (reserva.getId() == null){
            //verifica se existe reserva ou reserva rápida com a mesma placa e horário conflitante
            if (
                reservaRepository.existsByVeiculoPlacaIgnoringCaseAndStatusInAndFimGreaterThanAndInicioLessThan(placa, listaStatus, reserva.getInicio(), reserva.getFim())
                ||
                reservaRapidaRepository.existsByPlacaIgnoringCaseAndStatusInAndFimGreaterThanAndInicioLessThan(placa, listaStatus, reserva.getInicio(), reserva.getFim())
            ){
                throw new ReservaExceptions.PlacaComConflitoDeHorarioException();
            }

            //verifica se existe reserva com o mesmo veiculo(id) e horário conflitante
            if (reserva.getVeiculoId() != null ){
                if (reservaRepository.existsByVeiculoIdAndStatusInAndFimGreaterThanAndInicioLessThan(reserva.getVeiculoId(), listaStatus, reserva.getInicio(), reserva.getFim())){
                    throw new ReservaExceptions.VeiculoComConflitoDeHorarioException();
                }
            }
            //verifica se existe reserva com o mesmo motorista(id) e horário conflitante
            if (reserva.getMotoristaId() != null){
                if (reservaRepository.existsByMotoristaIdAndStatusInAndFimGreaterThanAndInicioLessThan(reserva.getMotoristaId(), listaStatus, reserva.getInicio(), reserva.getFim())){
                    throw new ReservaExceptions.MotoristaComConflitoDeHorarioException();
                }
            }
            
        } else {
            //verifica se existe uma reserva ou reserva rápida diferente com a mesma placa e horário conflitante
            if (
                reservaRepository.existsByIdNotAndVeiculoPlacaIgnoringCaseAndStatusInAndFimGreaterThanAndInicioLessThan(reserva.getId(), placa, listaStatus, reserva.getInicio(), reserva.getFim())
                ||
                reservaRapidaRepository.existsByIdNotAndPlacaIgnoringCaseAndStatusInAndFimGreaterThanAndInicioLessThan(reserva.getId(), placa, listaStatus, reserva.getInicio(), reserva.getFim())
            ){
                throw new ReservaExceptions.PlacaComConflitoDeHorarioException();
            }
            
            //verifica se existe uma reserva diferente com o mesmo veiculo(id) e horário conflitante
            if (reserva.getVeiculoId() != null ){
                if (reservaRepository.existsByIdNotAndVeiculoIdAndStatusInAndFimGreaterThanAndInicioLessThan(reserva.getId(), reserva.getVeiculoId(), listaStatus, reserva.getInicio(), reserva.getFim())){
                    throw new ReservaExceptions.VeiculoComConflitoDeHorarioException();
                }
            }
            //verifica se existe uma reserva diferente com o mesmo motorista(id) e horário conflitante
            if (reserva.getMotoristaId() != null){
                if (reservaRepository.existsByIdNotAndMotoristaIdAndStatusInAndFimGreaterThanAndInicioLessThan(reserva.getId(), reserva.getMotoristaId(), listaStatus, reserva.getInicio(), reserva.getFim())){
                    throw new ReservaExceptions.MotoristaComConflitoDeHorarioException();
                }
            }
        }
    }
    
    private void validarPosicaoPerpendicular(Integer quantidadePosicoesVaga, Integer posicaoPerpendicular) {
        if (quantidadePosicoesVaga == null || quantidadePosicoesVaga <= 0) {
            throw new VagaExceptions.QuantidadePosicoesInvalidaException();
        }

        if (posicaoPerpendicular == null || posicaoPerpendicular <= 0 || posicaoPerpendicular > quantidadePosicoesVaga) {
            throw new ReservaExceptions.PosicaoPerpendicularInvalidaException(quantidadePosicoesVaga);
        }

    }

    private void validarCapacidadePerpendicularPorPosicao(
        Integer comprimentoVaga,
        ReservaDTO reserva,
        List<ReservaDTO> reservasSobrepostasNaVaga,
        Integer posicaoPerpendicular
    ) {
        if (comprimentoVaga == null || comprimentoVaga <= 0) {
            throw new VagaExceptions.ComprimentoInvalidoException();
        }
        if (reserva.getTamanhoVeiculo() > comprimentoVaga) {
            throw new ReservaExceptions.VeiculoMaiorQueVagaException();
        }

        int ocupacaoAtual = reservasSobrepostasNaVaga != null && !reservasSobrepostasNaVaga.isEmpty() ?
                reservasSobrepostasNaVaga.stream()
                .filter(r -> posicaoPerpendicular.equals(r.getPosicaoPerpendicular()))
                .mapToInt(ReservaDTO::getTamanhoVeiculo)
                .sum() : 0;

        int ocupacaoFinal = ocupacaoAtual + reserva.getTamanhoVeiculo();

        if (ocupacaoFinal > comprimentoVaga) {
            throw new ReservaExceptions.EspacoInsuficienteNoPeriodoException();
        }
    }

    private void validarEspacoDisponivel(Vaga vaga, ReservaDTO reserva) {
        if (vaga.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)){
            List<ReservaDTO> reservasSobrepostasNaVaga = reservaUtils.getReservasAtivasSobrepostasNaVaga(reserva.getId(), vaga.getId(), reserva.getInicio(), reserva.getFim());
            Integer posicaoPerpendicular = reservaUtils.definirPosicaoVagaPerpendicular(vaga.getQuantidade(), vaga.getComprimento(), reserva, reservasSobrepostasNaVaga);
            reserva.setPosicaoPerpendicular(posicaoPerpendicular);
            validarPosicaoPerpendicular(vaga.getQuantidade(), reserva.getPosicaoPerpendicular());
            validarCapacidadePerpendicularPorPosicao(vaga.getComprimento(), reserva, reservasSobrepostasNaVaga, reserva.getPosicaoPerpendicular());
            return;
        } else {
            Integer tamanhoDisponivelVaga = vaga.getComprimento() - reserva.getTamanhoVeiculo();

            List<ReservaDTO> reservasSobrepostas = reservaUtils.getReservasAtivasSobrepostas(reserva.getId(), reserva.getInicio(), reserva.getFim());
            if (!reservasSobrepostas.isEmpty()){
                for (ReservaDTO reservaSobreposta : reservasSobrepostas){ 
                    if (tamanhoDisponivelVaga < 0 || (tamanhoDisponivelVaga - reservaSobreposta.getTamanhoVeiculo()) < 0) throw new ReservaExceptions.EspacoInsuficienteNoPeriodoException();
                    tamanhoDisponivelVaga -= reservaSobreposta.getTamanhoVeiculo();
                }
            }
        }
    }
    
}
