package com.cptrans.petrocarga.modules.reservaRapida.utils;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.reserva.exceptions.ReservaExceptions;
import com.cptrans.petrocarga.modules.reserva.repository.ReservaRepository;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.reservaRapida.dto.mapper.ReservaRapidaMapper;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.reservaRapida.repository.ReservaRapidaRepository;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;

@Component
public class ReservaRapidaUtils {
    @Autowired
    private ReservaUtils reservaUtils;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaRapidaRepository reservaRapidaRepository;

    public void validarQuantidadeReservasPorPlaca(ReservaDTO novaReserva) {
        Integer quantidadeReservasRapidasPorPlaca = reservaRapidaRepository.countByPlacaIgnoringCase(novaReserva.getPlacaVeiculo());
        if (quantidadeReservasRapidasPorPlaca >= ReservaUtils.LIMITE_DE_RESERVAS_POR_PLACA){
            throw new ReservaExceptions.LimiteDeReservasPorPlacaException(ReservaUtils.LIMITE_DE_RESERVAS_POR_PLACA);
        }

        List<ReservaDTO> reservasSobrepostas = getReservasAtivasSobrepostas(novaReserva.getInicio(), novaReserva.getFim());

        if (reservasSobrepostas != null && !reservasSobrepostas.isEmpty()  ){
            for (ReservaDTO reserva : reservasSobrepostas){
                if (reserva.getPlacaVeiculo().equals(novaReserva.getPlacaVeiculo()) ) {
                    throw new ReservaExceptions.PlacaComConflitoDeHorarioException();
                }
            } 
        }
    }


    public void validarEspacoDisponivelNaVaga(ReservaRapida novaReservaRapida, Vaga vagaReserva, List<ReservaDTO> reservasSobrepostas) {
        ReservaDTO novaReservaDTO = ReservaRapidaMapper.toReservaDTO(novaReservaRapida);

        reservaUtils.validarLimiteReservasPorPlaca(novaReservaDTO, reservasSobrepostas, ReservaUtils.METODO_POST);

        if (vagaReserva.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)) {
            ReservaUtils.validarPosicaoPerpendicular(vagaReserva.getTipoVaga(), vagaReserva.getQuantidade(), novaReservaRapida.getPosicaoPerpendicular());
            ReservaUtils.validarCapacidadePerpendicularPorPosicao(vagaReserva.getTipoVaga(), vagaReserva.getComprimento(), novaReservaDTO, reservasSobrepostas, novaReservaRapida.getPosicaoPerpendicular());
            return;
        }

        Integer tamanhoDisponivelVaga = vagaReserva.getComprimento() - novaReservaRapida.getTipoVeiculo().getComprimento();

        if (!reservasSobrepostas.isEmpty()) {
            for (ReservaDTO reserva : reservasSobrepostas) {
                validarReservaRapidaAtivaPorPlaca(reserva.getPlacaVeiculo(), novaReservaRapida.getPlaca());

                tamanhoDisponivelVaga -= reserva.getTamanhoVeiculo();

                if (tamanhoDisponivelVaga < 0) {
                    throw new ReservaExceptions.EspacoInsuficienteNoPeriodoException();
                }
            }
        }
    }

    public void validarReservaRapidaAtivaPorPlaca(String PlacaReservaAtiva, String PlacaNovaReserva) {
          if(PlacaReservaAtiva.equalsIgnoreCase(PlacaNovaReserva)) {
                throw new ReservaExceptions.PlacaComConflitoDeHorarioException();
            }

    }

    private List<ReservaDTO> getReservasAtivasSobrepostas(OffsetDateTime inicio, OffsetDateTime fim) {
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        List<Reserva> reservasNormaisSobrepostas = reservaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(inicio, fim, listaStatus);
        List<ReservaRapida> reservasRapidasSobrepostas = reservaRapidaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(inicio, fim, listaStatus);
        return ReservaUtils.juntarReservas(reservasNormaisSobrepostas, reservasRapidasSobrepostas);
    }
}