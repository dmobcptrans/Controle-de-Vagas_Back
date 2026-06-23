package com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;

@Component
public class EnderecoVagaMapper {
    public static EnderecoVagaResponseDTO toResponse(EnderecoVaga enderecoVaga) {
        if (enderecoVaga == null) return null;
        return new EnderecoVagaResponseDTO(
            enderecoVaga.getId(),
            enderecoVaga.getCodigoPmp(),
            enderecoVaga.getLogradouro(),
            enderecoVaga.getBairro());
    }

}