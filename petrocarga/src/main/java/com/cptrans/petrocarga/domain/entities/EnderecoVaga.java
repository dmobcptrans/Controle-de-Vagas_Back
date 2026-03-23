package com.cptrans.petrocarga.domain.entities;

import java.util.UUID;

import com.cptrans.petrocarga.application.dto.EnderecoVagaResponseDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "endereco_vaga")
public class EnderecoVaga {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false, length = 100, unique = true)
    @Size(min = 10, max = 255)
    private String logradouro;

    @Column(nullable = false, length = 50)
    @Size(min = 3, max = 50)
    private String bairro;

    @Column(nullable = false, length = 6, unique = true, name = "codigo_pmp")
    @Size(min = 1, max = 6)
    private String codigoPmp;

    public EnderecoVaga() {}

    public EnderecoVaga(String logradouro, String bairro, String codigoPmp) {
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.codigoPmp = codigoPmp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getCodigoPmp() {
        return codigoPmp;
    }

    public void setCodigoPmp(String codigoPmp) {
        this.codigoPmp = codigoPmp;
    }

    @Override
    public String toString() {
        return "EnderecoVaga{" +
                "id=" + id +
                ", logradouro='" + logradouro + '\'' +
                ", bairro='" + bairro + '\'' +
                ", codigoPmp='" + codigoPmp + '\'' +
                '}';
    }

    public EnderecoVagaResponseDTO toResponseDTO() {
        EnderecoVagaResponseDTO dto = new EnderecoVagaResponseDTO();
        dto.setId(this.id);
        dto.setCodigoPmp(this.codigoPmp);
        dto.setLogradouro(this.logradouro);
        dto.setBairro(this.bairro);
        return dto;
    }
}