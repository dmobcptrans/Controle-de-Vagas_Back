package com.cptrans.petrocarga.application.dto;

import com.cptrans.petrocarga.domain.entities.EnderecoVaga;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public class EnderecoVagaRequestDTO {
    @Valid
    @Size(min = 6, max = 6, message="O campo 'codigoPMP' deve ter 6 caracteres.")
    @Schema(
        description = "Código PMP do endereço da vaga",
        example = "Pb-123"
    )
    private String codigoPmp;

    @Valid
    @Schema(
        description = "Logradouro do endereço da vaga",
        example = "Rua Paulo Barbosa"
    )
    private String logradouro;

    @Valid
    @Schema(
        description = "Bairro do endereço da vaga",
        example = "Centro"
    )
    private String bairro;

    public EnderecoVaga toEntity() {
        EnderecoVaga enderecoVaga = new EnderecoVaga();
        enderecoVaga.setBairro(this.bairro);
        enderecoVaga.setCodigoPmp(this.codigoPmp);
        enderecoVaga.setLogradouro(this.logradouro);
        return enderecoVaga;
    }

    public String getCodigoPmp(){
        return this.codigoPmp;
    }

    public String getLogradouro(){
        return this.logradouro;
    }

    public String getBairro(){
        return this.bairro;
    }
}
