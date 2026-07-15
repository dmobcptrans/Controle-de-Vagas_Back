package com.cptrans.petrocarga.modules.gestor.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.gestor.dto.response.GestorResponseDTO;
import com.cptrans.petrocarga.modules.gestor.entity.Gestor;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GestorMapper {
    private final UsuarioMapper usuarioMapper;
    public GestorResponseDTO toResponse(Gestor gestor) {
        if (gestor == null) return null;
        Usuario gestorUsuario = gestor.getUsuario();
        return 
            new GestorResponseDTO(
                gestor.getId(),
                usuarioMapper.toResponse(gestorUsuario, gestor.getCpfCripto())
            );
    }
}