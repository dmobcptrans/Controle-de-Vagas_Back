package com.cptrans.petrocarga.modules.reserva.utils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.reserva.dto.mapper.ReservaMapper;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.reserva.exceptions.ReservaExceptions;
import com.cptrans.petrocarga.modules.reserva.repository.ReservaRepository;
import com.cptrans.petrocarga.modules.reservaRapida.dto.mapper.ReservaRapidaMapper;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.reservaRapida.repository.ReservaRapidaRepository;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.modules.vaga.exceptions.VagaExceptions;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class ReservaUtils {
    private final ReservaRepository reservaRepository;
    private final ReservaRapidaRepository reservaRapidaRepository;
    private final UsuarioUtils usuarioUtils;
    private final ReservaMapper reservaMapper;
    private final ReservaRapidaMapper reservaRapidaMapper;


    public List<ReservaDTO> getReservasAtivasSobrepostas(UUID reservaId, OffsetDateTime inicio, OffsetDateTime fim) {
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        if (reservaId != null){
            List<Reserva> reservasNormaisSobrepostas = reservaRepository.findByIdNotAndFimGreaterThanAndInicioLessThanAndStatusIn(reservaId, inicio, fim, listaStatus);
            List<ReservaRapida> reservasRapidasSobrepostas = reservaRapidaRepository.findByIdNotAndFimGreaterThanAndInicioLessThanAndStatusIn(reservaId, inicio, fim, listaStatus);
            return juntarReservas(reservasNormaisSobrepostas, reservasRapidasSobrepostas);
        } else {
            List<Reserva> reservasNormaisSobrepostas = reservaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(inicio, fim, listaStatus);
            List<ReservaRapida> reservasRapidasSobrepostas = reservaRapidaRepository.findByFimGreaterThanAndInicioLessThanAndStatusIn(inicio, fim, listaStatus);
            return juntarReservas(reservasNormaisSobrepostas, reservasRapidasSobrepostas);
        }
    }

    public List<ReservaDTO> getReservasAtivasSobrepostasNaVaga(UUID reservaId, UUID vagaId, OffsetDateTime inicio, OffsetDateTime fim) {
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA);
        if (reservaId != null){
            List<Reserva> reservasNormaisSobrepostas = reservaRepository.findByIdNotAndVagaIdAndFimGreaterThanAndInicioLessThanAndStatusIn(reservaId, vagaId, inicio, fim, listaStatus);
            List<ReservaRapida> reservasRapidasSobrepostas = reservaRapidaRepository.findByIdNotAndVagaIdAndFimGreaterThanAndInicioLessThanAndStatusIn(reservaId, vagaId, inicio, fim, listaStatus);
            return juntarReservas(reservasNormaisSobrepostas, reservasRapidasSobrepostas);
        } else {
            List<Reserva> reservasNormaisSobrepostas = reservaRepository.findByVagaIdAndFimGreaterThanAndInicioLessThanAndStatusIn(vagaId, inicio, fim, listaStatus);
            List<ReservaRapida> reservasRapidasSobrepostas = reservaRapidaRepository.findByVagaIdAndFimGreaterThanAndInicioLessThanAndStatusIn(vagaId, inicio, fim, listaStatus);
            return juntarReservas(reservasNormaisSobrepostas, reservasRapidasSobrepostas);
        }
    }

    public List<ReservaDTO> juntarReservas(List<Reserva> reservas, List<ReservaRapida> reservasRapidas) {
        List<ReservaDTO> listaFinalReservas = new ArrayList<>(); 

        if (reservasRapidas != null && !reservasRapidas.isEmpty()) {
            reservasRapidas.forEach((rr) -> {
                listaFinalReservas.add(reservaRapidaMapper.toReservaDTO(rr, rr.getAgente().getCpfCripto()));
            });
        }
    
        if (reservas != null && !reservas.isEmpty()) {
            reservas.forEach((r) -> {
                Usuario criadoPor = r.getCriadoPor();
                String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
                listaFinalReservas.add(reservaMapper.toReservaDTO(r, cpfOrCnpjCriador));
            });
        }
    
        return listaFinalReservas;
    }

    public Boolean existsAtivaByUsuarioId(UUID usuarioId) {
        return reservaRepository.existsAtivaByCriadoPorIdOrMotoristaId(usuarioId);
    }

    public Boolean existsAtivaByEmpresaIdAndMotoristaId(UUID empresaId, UUID motoristaId) {
        List<StatusReservaEnum> listaStatus = List.of(StatusReservaEnum.RESERVADA, StatusReservaEnum.ATIVA);
        Boolean existeCriadoPelaEmpresaEComMotorista = reservaRepository.existsByCriadoPorIdAndMotoristaIdAndStatusIn(empresaId, motoristaId, listaStatus);
        Boolean existeVeiculoDaEmpresaEComMotorista = reservaRepository.existsByVeiculoUsuarioIdAndMotoristaIdAndStatusIn(empresaId, motoristaId, listaStatus);
        return existeCriadoPelaEmpresaEComMotorista || existeVeiculoDaEmpresaEComMotorista;
    }

    public Integer definirPosicaoVagaPerpendicular(Integer quantidadePosicoesVaga, Integer comprimentoVaga, ReservaDTO reserva, List<ReservaDTO> reservasAtivasSobrepostasNaVaga) {
        if (quantidadePosicoesVaga == null || quantidadePosicoesVaga <= 0) {
            throw new VagaExceptions.QuantidadePosicoesInvalidaException();
        }
        if (reserva.getPosicaoPerpendicular() == null) {
            return encontrarPosicaoDisponivel(quantidadePosicoesVaga, comprimentoVaga, reserva, reservasAtivasSobrepostasNaVaga);
        } else {
            if (reserva.getPosicaoPerpendicular() <= 0 || reserva.getPosicaoPerpendicular() > quantidadePosicoesVaga) {
                return encontrarPosicaoDisponivel(quantidadePosicoesVaga, comprimentoVaga, reserva, reservasAtivasSobrepostasNaVaga);
            }
        }
        return reserva.getPosicaoPerpendicular();
    }

    private Integer encontrarPosicaoDisponivel(
        Integer quantidadePosicoesVaga,
        Integer comprimentoVaga,
        ReservaDTO reserva,
        List<ReservaDTO> reservasSobrepostasNaVaga
    ) {
        if (quantidadePosicoesVaga == null || quantidadePosicoesVaga <= 0) {
            throw new VagaExceptions.QuantidadePosicoesInvalidaException();
        }
        for (int posicao = 1; posicao <= quantidadePosicoesVaga; posicao++) {
            final int posicaoAtual = posicao;
            int ocupado = reservasSobrepostasNaVaga != null && !reservasSobrepostasNaVaga.isEmpty() ?
                reservasSobrepostasNaVaga.stream()
                    .filter(r -> posicaoAtual == r.getPosicaoPerpendicular())
                    .mapToInt(ReservaDTO::getTamanhoVeiculo)
                    .sum() : 0;

            if (ocupado + reserva.getTamanhoVeiculo() <= comprimentoVaga) {
                return posicaoAtual;
            }
        }

        throw new ReservaExceptions.TodasPosicoesOcupadasException();
    }
}