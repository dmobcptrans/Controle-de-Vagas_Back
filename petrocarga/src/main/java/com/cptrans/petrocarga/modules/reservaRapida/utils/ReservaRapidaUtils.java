package com.cptrans.petrocarga.modules.reservaRapida.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.exceptions.ReservaExceptions;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.reservaRapida.dto.mapper.ReservaRapidaMapper;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.reservaRapida.repository.ReservaRapidaRepository;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservaRapidaUtils {
    private final ReservaUtils reservaUtils;
    private final ReservaRapidaRepository reservaRapidaRepository;
    private final ReservaRapidaMapper reservaRapidaMapper;

    public void validarQuantidadeReservasPorPlaca(ReservaDTO novaReserva, List<ReservaDTO> reservasSobrepostas){
        Integer quantidadeReservasRapidasPorPlaca = reservaRapidaRepository.countByPlacaIgnoringCase(novaReserva.getPlacaVeiculo());
        if (quantidadeReservasRapidasPorPlaca >= ReservaUtils.LIMITE_DE_RESERVAS_POR_PLACA){
            throw new ReservaExceptions.LimiteDeReservasPorPlacaException(ReservaUtils.LIMITE_DE_RESERVAS_POR_PLACA);
        }
        
        if (
            reservasSobrepostas != null && 
            !reservasSobrepostas.isEmpty()  &&
            reservasSobrepostas.stream().anyMatch((r) -> r.getPlacaVeiculo().equals(novaReserva.getPlacaVeiculo()))
        ){
            throw new ReservaExceptions.PlacaComConflitoDeHorarioException();
        }
    }

    public void validarEspacoDisponivelNaVaga(ReservaRapida novaReservaRapida, Vaga vagaReserva, List<ReservaDTO> reservasSobrepostas) {
        ReservaDTO novaReservaDTO = reservaRapidaMapper.toReservaDTO(novaReservaRapida, novaReservaRapida.getAgente().getCpfCripto());

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

}