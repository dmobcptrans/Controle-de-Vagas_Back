package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.DisponibilidadeVaga;

public class DisponibilidadeVagaResponseDTO {

    private UUID id;
    private UUID vagaId;
    private EnderecoVagaResponseDTO endereco;
    private String referenciaEndereco;
    private String numeroEndereco;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private OffsetDateTime criadoEm;
    private UUID criadoPorId;

    public DisponibilidadeVagaResponseDTO() {
    }

    public DisponibilidadeVagaResponseDTO(DisponibilidadeVaga disponibilidadeVaga) {
        this.id = disponibilidadeVaga.getId();
        this.vagaId = disponibilidadeVaga.getVaga().getId();
        this.endereco = disponibilidadeVaga.getVaga().getEndereco().toResponseDTO();
        this.referenciaEndereco = disponibilidadeVaga.getVaga().getReferenciaEndereco();
        this.numeroEndereco = disponibilidadeVaga.getVaga().getNumeroEndereco();
        this.inicio = disponibilidadeVaga.getInicio();
        this.fim = disponibilidadeVaga.getFim();
        this.criadoEm = disponibilidadeVaga.getCriadoEm();
        this.criadoPorId = disponibilidadeVaga.getCriadoPor().getId();
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
    public EnderecoVagaResponseDTO getEndereco() {
        return endereco;
    }
    public void setEndereco(EnderecoVagaResponseDTO endereco) {
        this.endereco = endereco;
    }
    public String getReferenciaEndereco() {
        return referenciaEndereco;
    }
    public void setReferenciaEndereco(String referenciaEndereco) {
        this.referenciaEndereco = referenciaEndereco;
    }
    public String getNumeroEndereco() {
        return numeroEndereco;
    }
    public void setNumeroEndereco(String numeroEndereco) {
        this.numeroEndereco = numeroEndereco;
    }
    public void setVagaId(UUID vagaId) {
        this.vagaId = vagaId;
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
    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }
    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    public UUID getCriadoPorId() {
        return criadoPorId;
    }
    public void setCriadoPorId(UUID criadoPorId) {
        this.criadoPorId = criadoPorId;
    }
}
