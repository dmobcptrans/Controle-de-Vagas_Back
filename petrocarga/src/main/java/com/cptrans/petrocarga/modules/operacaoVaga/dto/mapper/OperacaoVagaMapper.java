package com.cptrans.petrocarga.modules.operacaoVaga.dto.mapper;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.DiaSemanaEnum;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.request.OperacaoVagaRequestDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.response.OperacaoVagaResponseDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;

@Component
public class OperacaoVagaMapper {
    private Comparator<OperacaoVagaResponseDTO> compararPorCodigoEnum = Comparator.comparingInt(op -> op.getDiaSemanaAsEnum().getCodigo());
    
    public OperacaoVaga toEntity(OperacaoVagaRequestDTO request, Vaga vaga) {
        if (request == null) return null;
        return new OperacaoVaga(
            vaga,
            DiaSemanaEnum.toEnumByCodigo(request.getCodigoDiaSemana()),
            request.getHoraInicio(),
            request.getHoraFim()
        );
    }

    public Set<OperacaoVaga> toEntitySet(Set<OperacaoVagaRequestDTO> request, Vaga vaga) {
        if (request == null || request.isEmpty()) return operacoesVagaDefault(vaga);
        return request.stream().map(operacaoVagaRequest -> toEntity(operacaoVagaRequest, vaga)).collect(Collectors.toSet());
    }

    public OperacaoVagaResponseDTO toResponse(OperacaoVaga operacaoVaga) {
        if (operacaoVaga == null) return null;
        OperacaoVagaResponseDTO response = new OperacaoVagaResponseDTO(
            operacaoVaga.getId(),
            operacaoVaga.getDiaSemana(),
            operacaoVaga.getHoraInicio(),
            operacaoVaga.getHoraFim()
        );
        return response;
    }

    public Set<OperacaoVagaResponseDTO> toResponseSet(Set<OperacaoVaga> operacoesVaga) {
        if (operacoesVaga == null || operacoesVaga.isEmpty()) return null;
        return operacoesVaga.stream().map(this::toResponse).collect(Collectors.toCollection(() -> new TreeSet<>(compararPorCodigoEnum)));
    }

    private Set<OperacaoVaga> operacoesVagaDefault(Vaga vaga) {
        Set<OperacaoVaga> operacoesVaga = new HashSet<>();
        final int HORARIO_DEFAULT_INICIO = 0;
        final int HORARIO_DEFAULT_FIM = 13;
        for (int i = 1; i <= 7; i++) {
            OperacaoVaga operacaoVaga = new OperacaoVaga(
                vaga,
                DiaSemanaEnum.toEnumByCodigo(i),
                LocalTime.of(HORARIO_DEFAULT_INICIO, 00),
                LocalTime.of(HORARIO_DEFAULT_FIM, 00)
            );
            operacoesVaga.add(operacaoVaga);
        }
        return operacoesVaga;
    }
}