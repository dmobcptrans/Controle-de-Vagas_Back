package com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.mapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.response.DisponibilidadeVagaResponseDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.response.DisponibilidadeVagaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.entity.DisponibilidadeVaga;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DisponibilidadeVagaMapper {
    private final EnderecoVagaMapper enderecoVagaMapper;

    public DisponibilidadeVaga toEntity(OffsetDateTime inicio, OffsetDateTime fim, Vaga vaga, UUID criadoPorId){
        return new DisponibilidadeVaga(
            vaga,
            DateUtils.fusoHorarioBrasilia(inicio),
            DateUtils.fusoHorarioBrasilia(fim),
            criadoPorId
        );
    }

    public DisponibilidadeVagaResponseDTO toResponse(DisponibilidadeVaga disponibilidadeVaga) {
        if (disponibilidadeVaga == null) return null;
        Vaga vaga = disponibilidadeVaga.getVaga();
        EnderecoVaga enderecoVaga = vaga != null ? vaga.getEndereco() : null;
        return new DisponibilidadeVagaResponseDTO(
            disponibilidadeVaga.getId(),
            vaga != null ? vaga.getId() : null,
            enderecoVagaMapper.toResponse(enderecoVaga),
            vaga != null ? vaga.getReferenciaEndereco() : null,
            vaga != null ? vaga.getNumeroEndereco() : null,
            disponibilidadeVaga.getInicio(),
            disponibilidadeVaga.getFim(),
            disponibilidadeVaga.getCriadoEm(),
            disponibilidadeVaga.getCriadoPorId()
        );
    }

    public  List<DisponibilidadeVagaResponseDTO> toResponseList(List<DisponibilidadeVaga> disponibilidadeVagas) {
        if (disponibilidadeVagas == null || disponibilidadeVagas.isEmpty()) return List.of();
        return disponibilidadeVagas.stream().map(this::toResponse).toList();
    }

    public DisponibilidadeVagaSimplificadoResponseDTO toResponseSimplificado(DisponibilidadeVaga disponibilidadeVaga) {
        if (disponibilidadeVaga == null) return null;
        return new DisponibilidadeVagaSimplificadoResponseDTO(disponibilidadeVaga.getId(), disponibilidadeVaga.getInicio(), disponibilidadeVaga.getFim());
    }

    public List<DisponibilidadeVagaSimplificadoResponseDTO> toResponseSimplificadoList(List<DisponibilidadeVaga> disponibilidadeVagas) {
        if (disponibilidadeVagas == null || disponibilidadeVagas.isEmpty()) return List.of();
        return disponibilidadeVagas.stream().map(this::toResponseSimplificado).toList();
    }
}
