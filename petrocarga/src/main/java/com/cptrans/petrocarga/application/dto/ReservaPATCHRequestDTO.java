package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Size;

public class ReservaPATCHRequestDTO {
    private UUID veiculoId;
    @Size(min = 10, max = 100, message = "Cidade Origem deve ter entre 10 e 100 caracteres.")
    private String cidadeOrigem;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    
    public UUID getVeiculoId () {
        return veiculoId;
    }

    public String getCidadeOrigem () {
        return cidadeOrigem;
    }

    public OffsetDateTime getInicio () {
        return inicio;
    }

    public OffsetDateTime getFim () {
        return fim;
    }
}
