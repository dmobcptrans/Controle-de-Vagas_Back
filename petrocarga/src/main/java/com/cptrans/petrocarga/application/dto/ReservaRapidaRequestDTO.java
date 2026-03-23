package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.TipoVeiculoEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReservaRapidaRequestDTO {

    @NotNull
    private UUID vagaId;

    @Valid
    private TipoVeiculoEnum tipoVeiculo;

    @Size(min = 7, max = 10, message = "Placa deve ter entre 7 e 10 caracteres.")
    private String placa;

    @NotNull
    private OffsetDateTime inicio;

    @NotNull
    private OffsetDateTime fim;

    public ReservaRapidaRequestDTO() {
        
    }

    public ReservaRapida toEntity(Vaga vaga) {
        ReservaRapida reservaRapida = new ReservaRapida();
        reservaRapida.setVaga(vaga);
        reservaRapida.setTipoVeiculo(this.tipoVeiculo);
        reservaRapida.setPlaca(this.placa != null ? this.placa.trim().toUpperCase() : null);
        reservaRapida.setInicio(this.inicio);
        reservaRapida.setFim(this.fim);
        return reservaRapida;
    }

    // Getters and Setters
    public UUID getVagaId() {
        return vagaId;
    }

    public void setVagaId(UUID vagaId) {
        this.vagaId = vagaId;
    }

    public TipoVeiculoEnum getTipoVeiculo() {
        return tipoVeiculo;
    }

    public void setTipoVeiculo(TipoVeiculoEnum tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public OffsetDateTime getInicio() {
        return inicio;
    }

    public void setInicio(OffsetDateTime inicio) {
        this.inicio = inicio;
    }

    public OffsetDateTime getFim() {
        return fim;
    }

    public void setFim(OffsetDateTime fim) {
        this.fim = fim;
    }
}