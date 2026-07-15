package com.cptrans.petrocarga.modules.agente.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.agente.dto.response.AgenteResponseDTO;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AgenteMapper {
    private final UsuarioMapper usuarioMapper;

    public AgenteResponseDTO toResponse(Agente agente) {
        if (agente == null) return null;
        return new AgenteResponseDTO(agente.getId(), usuarioMapper.toResponse(agente.getUsuario(), agente.getCpfCripto()), agente.getMatricula());
    }
}