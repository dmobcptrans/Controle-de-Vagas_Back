package com.cptrans.petrocarga.application.dto;

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
