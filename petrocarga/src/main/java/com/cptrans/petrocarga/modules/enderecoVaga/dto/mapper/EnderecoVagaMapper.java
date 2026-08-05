package com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.request.EnderecoVagaRequestDTO;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.shared.utils.StringUtils;

@Component
public class EnderecoVagaMapper {

    public EnderecoVaga toEntity(EnderecoVagaRequestDTO request) {
        if (request == null) return null;
        return new EnderecoVaga(
            StringUtils.formatarNome(request.getLogradouro().trim()), 
            StringUtils.formatarNome(request.getBairro().trim()), 
            request.getCodigoPmp().trim().toUpperCase()
        );
    }

    public EnderecoVagaResponseDTO toResponse(EnderecoVaga enderecoVaga) {
        if (enderecoVaga == null) return null;
        return new EnderecoVagaResponseDTO(
            enderecoVaga.getId(),
            enderecoVaga.getCodigoPmp(),
            enderecoVaga.getLogradouro(),
            enderecoVaga.getBairro());
    }

    public List<EnderecoVagaResponseDTO> toResponseList(List<EnderecoVaga> enderecosVaga) {
        if (enderecosVaga == null || enderecosVaga.isEmpty()) return List.of();
        return enderecosVaga.stream().map(this::toResponse).toList();
    }

}