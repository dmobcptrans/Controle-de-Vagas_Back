package com.cptrans.petrocarga.modules.enderecoVaga.entity;

import java.util.UUID;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "endereco_vaga")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
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

    public EnderecoVaga(String logradouro, String bairro, String codigoPmp) {
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.codigoPmp = codigoPmp;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public void setCodigoPmp(String codigoPmp) {
        this.codigoPmp = codigoPmp;
    }
}