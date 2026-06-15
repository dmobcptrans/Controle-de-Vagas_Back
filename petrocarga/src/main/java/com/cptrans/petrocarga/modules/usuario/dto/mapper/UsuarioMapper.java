package com.cptrans.petrocarga.modules.usuario.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;

@Component
public class UsuarioMapper {
    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(usuario);
    }
}
