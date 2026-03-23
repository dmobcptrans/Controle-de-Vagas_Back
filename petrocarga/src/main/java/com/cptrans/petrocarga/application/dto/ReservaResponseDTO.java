package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;

public class ReservaResponseDTO {

    private UUID id;
    private UUID vagaId;
    private String logradouro;
    private String bairro;
    private UUID motoristaId;
    private UUID veiculoId;
    private UUID criadoPorId;
    private String cidadeOrigem;
    private String entradaCidade;
    private OffsetDateTime criadoEm;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private StatusReservaEnum status;
    private Boolean checkedIn;
    private OffsetDateTime checkInEm;
    private OffsetDateTime checkOutEm;

    public ReservaResponseDTO() {
    }

    public ReservaResponseDTO(Reserva reserva) {
        this.id = reserva.getId();
        this.vagaId = reserva.getVaga().getId();
        this.logradouro = reserva.getVaga().getEndereco().getLogradouro();
        this.bairro = reserva.getVaga().getEndereco().getBairro();
        this.motoristaId = reserva.getMotorista().getId();
        this.veiculoId = reserva.getVeiculo().getId();
        this.criadoPorId = reserva.getCriadoPor().getId();
        this.cidadeOrigem = reserva.getCidadeOrigem();
        this.entradaCidade = reserva.getEntradaCidade();
        this.criadoEm = reserva.getCriadoEm();
        this.inicio = reserva.getInicio();
        this.fim = reserva.getFim();
        this.status = reserva.getStatus();
        this.checkedIn = reserva.isCheckedIn();
        this.checkInEm = reserva.getCheckInEm();
        this.checkOutEm = reserva.getCheckOutEm();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public void setVagaId(UUID vagaId) {
        this.vagaId = vagaId;
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

    public UUID getMotoristaId() {
        return motoristaId;
    }

    public void setMotoristaId(UUID motoristaId) {
        this.motoristaId = motoristaId;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(UUID veiculoId) {
        this.veiculoId = veiculoId;
    }

    public UUID getCriadoPorId() {
        return criadoPorId;
    }

    public void setCriadoPorId(UUID criadoPorId) {
        this.criadoPorId = criadoPorId;
    }

    public String getCidadeOrigem() {
        return cidadeOrigem;
    }

    public void setCidadeOrigem(String cidadeOrigem) {
        this.cidadeOrigem = cidadeOrigem;
    }

    public String getEntradaCidade() {
        return entradaCidade;
    }

    public void setEntradaCidade(String entradaCidade) {
        this.entradaCidade = entradaCidade;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
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

    public StatusReservaEnum getStatus() {
        return status;
    }

    public void setStatus(StatusReservaEnum status) {
        this.status = status;
    }

    public Boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public OffsetDateTime getCheckInEm() {
        return checkInEm;
    }

    public void setCheckInEm(OffsetDateTime checkInEm) {
        this.checkInEm = checkInEm;
    }

    public OffsetDateTime getCheckOutEm() {
        return checkOutEm;
    }

    public void setCheckOutEm(OffsetDateTime checkOutEm) {
        this.checkOutEm = checkOutEm;
    }
}
