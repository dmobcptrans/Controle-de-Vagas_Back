package com.cptrans.petrocarga.application.dto;

import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.EnderecoVaga;

public class EnderecoVagaResponseDTO {
    private UUID id;
    private String codigoPmp;
    private String logradouro;  
    private String bairro;

    public EnderecoVagaResponseDTO() {
    }

    public EnderecoVagaResponseDTO(EnderecoVaga enderecoVaga) {
        this.id = enderecoVaga.getId();
        this.codigoPmp = enderecoVaga.getCodigoPmp();
        this.logradouro = enderecoVaga.getLogradouro();
        this.bairro = enderecoVaga.getBairro();
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getCodigoPmp() {
        return codigoPmp;
    }
    public void setCodigoPmp(String codigoPmp) {
        this.codigoPmp = codigoPmp;
    }
    public String getLogradouro() {
        return logradouro;
    }
    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }
    public String getBairro() {
        return bairro;
    }
    public void setBairro(String bairro) {
        this.bairro = bairro;
    }
}
