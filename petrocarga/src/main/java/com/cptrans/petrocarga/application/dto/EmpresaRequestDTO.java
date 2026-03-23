package com.cptrans.petrocarga.application.dto;

import java.util.UUID;

import org.hibernate.validator.constraints.br.CNPJ;

import com.cptrans.petrocarga.domain.entities.Empresa;
import com.cptrans.petrocarga.domain.entities.Usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EmpresaRequestDTO {

    @NotNull
    private UUID usuarioId;

    @NotBlank
    @Valid
    @CNPJ(message = "Informe um CNPJ válido.")
    private String cnpj;

    @NotBlank
    private String razaoSocial;

    public Empresa toEntity(Usuario usuario) {
        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setCnpj(this.cnpj);
        empresa.setRazaoSocial(this.razaoSocial);
        return empresa;
    }

    // Getters and Setters
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
