package com.cptrans.petrocarga.modules.operacaoVaga.dto.mapper;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.operacaoVaga.dto.response.OperacaoVagaResponseDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;

@Component
public class OperacaoVagaMapper {
    private static Comparator<OperacaoVagaResponseDTO> compararPorCodigoEnum = Comparator.comparingInt(op -> op.getDiaSemanaAsEnum().getCodigo());
    
    public static OperacaoVagaResponseDTO toResponse(OperacaoVaga operacaoVaga) {
        if (operacaoVaga == null) return null;
        OperacaoVagaResponseDTO response = new OperacaoVagaResponseDTO(
            operacaoVaga.getId(),
            operacaoVaga.getDiaSemana(),
            operacaoVaga.getHoraInicio(),
            operacaoVaga.getHoraFim()
        );
        return response;
    }

    public static Set<OperacaoVagaResponseDTO> toResponseSet(Set<OperacaoVaga> operacoesVaga) {
        if (operacoesVaga == null || operacoesVaga.isEmpty()) return null;
        return operacoesVaga.stream().map(OperacaoVagaMapper::toResponse).collect(Collectors.toCollection(() -> new TreeSet<>(compararPorCodigoEnum)));
    }
}
