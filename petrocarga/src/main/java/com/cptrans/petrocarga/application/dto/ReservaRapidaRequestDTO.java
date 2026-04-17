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

    @Size(min = 7, max = 7, message = "Placa deve ter 7 caracteres.")
    private String placa;

    @NotNull
    private OffsetDateTime inicio;

    @NotNull
    private OffsetDateTime fim;

    private Integer posicaoPerpendicular;

    private String cidadeOrigem;

    private String entradaCidade;

    public ReservaRapidaRequestDTO() {
        
    }

    public ReservaRapida toEntity(Vaga vaga) {
        ReservaRapida reservaRapida = new ReservaRapida();
        reservaRapida.setVaga(vaga);
        reservaRapida.setTipoVeiculo(this.tipoVeiculo);
        reservaRapida.setPlaca(this.placa != null ? this.placa.trim().toUpperCase() : null);
        reservaRapida.setInicio(this.inicio);
        reservaRapida.setFim(this.fim);
        reservaRapida.setPosicaoPerpendicular(this.posicaoPerpendicular);
        reservaRapida.setCidadeOrigem(this.cidadeOrigem);
        reservaRapida.setEntradaCidade(this.entradaCidade);
        return reservaRapida;
    }

    // Getters and Setters
    public UUID getVagaId() {
        return vagaId;
    }

    public TipoVeiculoEnum getTipoVeiculo() {
        return tipoVeiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public OffsetDateTime getInicio() {
        return inicio;
    }

    public OffsetDateTime getFim() {
        return fim;
    }

    public Integer getPosicaoPerpendicular() {
        return posicaoPerpendicular;
    }

    public String getCidadeOrigem() {
        return cidadeOrigem;
    }

    public String getEntradaCidade() {
        return entradaCidade;
    }
}