package com.cptrans.petrocarga.modules.gestor.dto.response;

import java.util.UUID;

import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class GestorResponseDTO {
    private UUID id;
    private UsuarioResponseDTO usuario;
}