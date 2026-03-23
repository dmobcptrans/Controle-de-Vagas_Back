package com.cptrans.petrocarga.application.dto;

import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Agente;

public class AgenteResponseDTO {
    private UUID id;
    private UsuarioResponseDTO usuario;
    private String matricula;

    public AgenteResponseDTO() {}

    public AgenteResponseDTO(Agente agente) {
        this.id = agente.getId();
        this.usuario = agente.getUsuario().toResponseDTO();
        this.matricula = agente.getMatricula();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public UsuarioResponseDTO getUsuario() {
        return usuario;
    }
    public void setUsuario(UsuarioResponseDTO usuario) {
        this.usuario = usuario;
    }
    public String getMatricula() {
        return matricula;
    }
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
