package com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.response.DisponibilidadeVagaResponseDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.entity.DisponibilidadeVaga;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;

@Component
public class DisponibilidadeVagaMapper {
    public static DisponibilidadeVagaResponseDTO toResponse(DisponibilidadeVaga disponibilidadeVaga) {
        if (disponibilidadeVaga == null) return null;
        Vaga vaga = disponibilidadeVaga.getVaga();
        EnderecoVaga enderecoVaga = vaga != null ? vaga.getEndereco() : null;
        Usuario criadoPor = disponibilidadeVaga.getCriadoPor();
        return new DisponibilidadeVagaResponseDTO(
            disponibilidadeVaga.getId(),
            vaga != null ? vaga.getId() : null,
            EnderecoVagaMapper.toResponse(enderecoVaga),
            vaga != null ? vaga.getReferenciaEndereco() : null,
            vaga != null ? vaga.getNumeroEndereco() : null,
            disponibilidadeVaga.getInicio(),
            disponibilidadeVaga.getFim(),
            disponibilidadeVaga.getCriadoEm(),
            criadoPor != null ? criadoPor.getId() : null
        );
    }

    public static List<DisponibilidadeVagaResponseDTO> toResponseList(List<DisponibilidadeVaga> disponibilidadeVagas) {
        if (disponibilidadeVagas == null || disponibilidadeVagas.isEmpty()) return null;
        return disponibilidadeVagas.stream().map(DisponibilidadeVagaMapper::toResponse).toList();
    }
}
