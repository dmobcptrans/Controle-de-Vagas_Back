package com.cptrans.petrocarga.modules.agente.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.agente.dto.response.AgenteResponseDTO;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;

@Component
public class AgenteMapper {
    public static AgenteResponseDTO toResponse(Agente agente) {
        if (agente == null) return null;
        return new AgenteResponseDTO(agente.getId(), UsuarioMapper.toResponse(agente.getUsuario()), agente.getMatricula());
    }
}
