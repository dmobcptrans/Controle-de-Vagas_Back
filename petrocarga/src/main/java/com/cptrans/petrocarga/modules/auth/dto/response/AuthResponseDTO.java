package com.cptrans.petrocarga.modules.auth.dto.response;

import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;

public class AuthResponseDTO {
    UsuarioResponseDTO usuario;
    String token;

    public AuthResponseDTO(UsuarioResponseDTO usuario, String token) {
        this.usuario = usuario;
        this.token = token;
    }
    public UsuarioResponseDTO getUsuario() {
        return usuario;
    }
    public void setUsuario(UsuarioResponseDTO usuario) {
        this.usuario = usuario;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}