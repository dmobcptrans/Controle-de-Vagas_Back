package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.DisponibilidadeVaga;

import jakarta.validation.Valid;

public class MultiplasDisponibilidadesVagaRequestDTO {
    @Valid
    private List<UUID> listaVagaId;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;

    public DisponibilidadeVaga toEntity(){
        DisponibilidadeVaga disponibilidadeVaga = new DisponibilidadeVaga();
        disponibilidadeVaga.setInicio(this.inicio);
        disponibilidadeVaga.setFim(this.fim);
        return disponibilidadeVaga;
        
    }

    public List<UUID> getListaVagaId() {
        return listaVagaId;
    }
    public OffsetDateTime getInicio() {
        return inicio;
    }
    public OffsetDateTime getFim() {
        return fim;
    }
    
}
