package com.cptrans.petrocarga.modules.operacaoVaga.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.DiaSemanaEnum;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.mapper.OperacaoVagaMapper;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.request.OperacaoVagaRequestDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.response.OperacaoVagaResponseDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.cptrans.petrocarga.modules.operacaoVaga.repository.OperacaoVagaRepository;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperacaoVagaService {

    private final OperacaoVagaRepository operacaoVagaRepository;
    private final OperacaoVagaMapper operacaoVagaMapper;

    public List<OperacaoVaga> salvarOperacoesVaga(Set<OperacaoVaga> listaOperacaoVaga) {
        return operacaoVagaRepository.saveAll(listaOperacaoVaga);
    }

    public Set<OperacaoVagaResponseDTO> findByVagaId(UUID vagaId) {
        Set<OperacaoVaga> setOperacoesVaga = operacaoVagaRepository.findByVagaId(vagaId);
        return operacaoVagaMapper.toResponseSet(setOperacoesVaga);
    }

    public void atualizarOperacoesVaga(Set<OperacaoVagaRequestDTO> request, Vaga vaga){
        Map<DiaSemanaEnum, OperacaoVaga> mapaExistentes = vaga.getOperacoesVaga()
                .stream()
                .collect(Collectors.toMap(OperacaoVaga::getDiaSemana, o -> o));

        Map<DiaSemanaEnum, OperacaoVaga> mapaNovas = operacaoVagaMapper.toEntitySet(request, vaga)
                .stream()
                .collect(Collectors.toMap(OperacaoVaga::getDiaSemana, o -> o, (o1, o2) -> o1)); // caso venha duplicado, mantém o primeiro

        for (OperacaoVaga novaOperacao : mapaNovas.values()) {
            OperacaoVaga existente = mapaExistentes.get(novaOperacao.getDiaSemana());
            if (existente != null) {
                existente.setHoraInicio(novaOperacao.getHoraInicio());
                existente.setHoraFim(novaOperacao.getHoraFim());
            } else {
                novaOperacao.setVaga(vaga);
                vaga.getOperacoesVaga().add(novaOperacao);
            }
        }

        vaga.getOperacoesVaga().removeIf(
            operacao -> !mapaNovas.containsKey(operacao.getDiaSemana())
        );
    }
}