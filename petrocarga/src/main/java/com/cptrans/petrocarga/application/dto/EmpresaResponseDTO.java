package com.cptrans.petrocarga.application.dto;

import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Empresa;

public class EmpresaResponseDTO {

    private UUID id;
    private UUID usuarioId;
    private String cnpj;
    private String razaoSocial;

    public EmpresaResponseDTO() {
    }

    public EmpresaResponseDTO(Empresa empresa) {
        this.id = empresa.getId();
        this.usuarioId = empresa.getUsuario().getId();
        this.cnpj = empresa.getCnpj();
        this.razaoSocial = empresa.getRazaoSocial();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }
}
