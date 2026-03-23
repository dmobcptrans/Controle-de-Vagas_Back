package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.DisponibilidadeVaga;

import jakarta.validation.constraints.NotNull;

public class DisponibilidadeVagaRequestDTO {

    @NotNull
    private UUID vagaId;

    @NotNull
    private OffsetDateTime inicio;

    @NotNull
    private OffsetDateTime fim;

    public DisponibilidadeVaga toEntity() {
        DisponibilidadeVaga disponibilidadeVaga = new DisponibilidadeVaga();
        disponibilidadeVaga.setInicio(this.inicio);
        disponibilidadeVaga.setFim(this.fim);
        return disponibilidadeVaga;
    }

    // Getters
    public UUID getVagaId() {
        return vagaId;
    }
    public OffsetDateTime getInicio() {
        return inicio;
    }
    public OffsetDateTime getFim() {
        return fim;
    }
}
