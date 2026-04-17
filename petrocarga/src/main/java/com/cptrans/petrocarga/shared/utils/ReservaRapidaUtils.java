package com.cptrans.petrocarga.shared.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.application.dto.ReservaDTO;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVagaEnum;
import com.cptrans.petrocarga.domain.repositories.ReservaRapidaRepository;
import com.cptrans.petrocarga.domain.repositories.ReservaRepository;

@Component
public class ReservaRapidaUtils {
    @Autowired
    private ReservaUtils reservaUtils;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaRapidaRepository reservaRapidaRepository;

    public static final Integer  LIMITE_DE_RESERVAS_POR_PLACA = 3;

    public void validarQuantidadeReservasPorPlaca( ReservaDTO novaReserva) {
        Integer quantidadeReservasRapidasPorPlaca = reservaRapidaRepository.countByPlacaIgnoringCase(novaReserva.getPlacaVeiculo());
        if(quantidadeReservasRapidasPorPlaca >= LIMITE_DE_RESERVAS_POR_PLACA ){
            throw new IllegalArgumentException("Veículo com placa " + novaReserva.getPlacaVeiculo() + " já atingiu o limite de " + LIMITE_DE_RESERVAS_POR_PLACA + " reservas rápidas. " + "Para novas reservas, o responsável deve realizar cadastro como motorista.");
        }
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        List<Reserva> reservasNormaisSobrepostas = reservaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(novaReserva.getInicio(), novaReserva.getFim(), listaStatus);
        List<ReservaRapida> reservasRapidasSobrepostas = reservaRapidaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(novaReserva.getInicio(), novaReserva.getFim(), listaStatus);
        List<ReservaDTO> reservasSobrepostas = ReservaUtils.juntarReservas(reservasNormaisSobrepostas, reservasRapidasSobrepostas);

        if(reservasSobrepostas != null && !reservasSobrepostas.isEmpty()  ){
            for(ReservaDTO reserva : reservasSobrepostas){
                if(reserva.getPlacaVeiculo().equals(novaReserva.getPlacaVeiculo()) ) {
                    throw new IllegalArgumentException("Veículo de placa " + novaReserva.getPlacaVeiculo() + " ja possui uma reserva com status: " + reserva.getStatus() + " com inicio: " + reserva.getInicio().atZoneSameInstant(DateUtils.FUSO_BRASIL) + " e fim: " + reserva.getFim().atZoneSameInstant(DateUtils.FUSO_BRASIL) + ".");
                }
            } 
        }
    }


    public void validarEspacoDisponivelNaVaga(ReservaRapida novaReservaRapida, Vaga vagaReserva, List<ReservaDTO> reservasAtivasNaVaga) {
        ReservaDTO novaReservaDTO = novaReservaRapida.toReservaDTO();

        reservaUtils.validarLimiteReservasPorPlaca(novaReservaDTO, ReservaUtils.METODO_POST);

        if (vagaReserva.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)) {
            ReservaUtils.validarPosicaoPerpendicular(vagaReserva.getTipoVaga(), vagaReserva.getQuantidade(), novaReservaRapida.getPosicaoPerpendicular());
            ReservaUtils.validarCapacidadePerpendicularPorPosicao(vagaReserva, novaReservaDTO, reservasAtivasNaVaga, novaReservaRapida.getPosicaoPerpendicular());
            return;
        }

        Integer tamanhoDisponivelVaga = vagaReserva.getComprimento() - novaReservaRapida.getTipoVeiculo().getComprimento();

        if (!reservasAtivasNaVaga.isEmpty()) {
            for (ReservaDTO reserva : reservasAtivasNaVaga) {
                Boolean reservaSobrepostas =
                    novaReservaRapida.getInicio().toInstant().isBefore(reserva.getFim().toInstant()) &&
                    novaReservaRapida.getFim().toInstant().isAfter(reserva.getInicio().toInstant());

                if (reservaSobrepostas) {
                    validarReservaRapidaAtivaPorPlaca(reserva.getPlacaVeiculo(), novaReservaRapida.getPlaca());

                    tamanhoDisponivelVaga -= reserva.getTamanhoVeiculo();

                    if (tamanhoDisponivelVaga < 0) {
                        throw new IllegalArgumentException(
                            "Não há espaço suficiente na vaga para a reserva no período solicitado devido a ocupações existentes. Espaço disponível: "
                            + (tamanhoDisponivelVaga + novaReservaRapida.getTipoVeiculo().getComprimento()) + " metros."
                        );
                    }
                }
            }
        }
    }

    public void validarReservaRapidaAtivaPorPlaca(String PlacaReservaAtiva, String PlacaNovaReserva) {
          if(PlacaReservaAtiva.equalsIgnoreCase(PlacaNovaReserva)) {
                throw new IllegalArgumentException("Veículo com placa " + PlacaNovaReserva + " já possui uma reserva ativa nesta vaga.");
            }

    }

    public Integer getLIMITE_DE_RESERVAS_POR_PLACA() {
        return LIMITE_DE_RESERVAS_POR_PLACA;
    }
}