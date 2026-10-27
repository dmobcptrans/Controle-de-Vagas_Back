package com.cptrans.petrocarga.modules.operacaoVaga.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

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

    public void atualizarOperacoesVaga(Set<OperacaoVagaRequestDTO> request, Vaga vaga) {
        Set<OperacaoVaga> novasOperacoes = operacaoVagaMapper.toEntitySet(request, vaga);
        vaga.getOperacoesVaga().clear();
        for (OperacaoVaga operacao : novasOperacoes) {
            if (operacao.getHoraInicio() == null || operacao.getHoraFim() == null) continue;
            if (!operacao.getHoraFim().isAfter(operacao.getHoraInicio())) continue;
            vaga.getOperacoesVaga().add(operacao);
        };
    }
}