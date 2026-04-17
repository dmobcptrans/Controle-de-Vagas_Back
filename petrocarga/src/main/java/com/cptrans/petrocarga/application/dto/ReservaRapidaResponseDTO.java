package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVeiculoEnum;

public class ReservaRapidaResponseDTO {

    private UUID id;
    private UUID vagaId;
    private UUID agenteId;
    private String logradouro;
    private String bairro;
    private TipoVeiculoEnum tipoVeiculo;
    private String placa;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private OffsetDateTime criadoEm;
    private StatusReservaEnum status;
    private Integer posicaoPerpendicular;
    private String cidadeOrigem;
    private String entradaCidade;

    public ReservaRapidaResponseDTO() {
    }

    public ReservaRapidaResponseDTO(ReservaRapida reservaRapida) {
        this.id = reservaRapida.getId();
        this.vagaId = reservaRapida.getVaga().getId();
        this.agenteId = reservaRapida.getAgente().getId();
        this.logradouro = reservaRapida.getVaga().getEndereco().getLogradouro();
        this.bairro = reservaRapida.getVaga().getEndereco().getBairro();
        this.tipoVeiculo = reservaRapida.getTipoVeiculo();
        this.placa = reservaRapida.getPlaca();
        this.inicio = reservaRapida.getInicio();
        this.fim = reservaRapida.getFim();
        this.criadoEm = reservaRapida.getCriadoEm();
        this.status = reservaRapida.getStatus();
        this.posicaoPerpendicular = reservaRapida.getPosicaoPerpendicular();
        this.cidadeOrigem = reservaRapida.getCidadeOrigem();
        this.entradaCidade = reservaRapida.getEntradaCidade();
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

    public UUID getAgenteId() {
        return agenteId;
    }

    public void setAgenteId(UUID agenteId) {
        this.agenteId = agenteId;
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

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    public StatusReservaEnum getStatus() {
        return status;
    }
    public void setStatus(StatusReservaEnum status) {
        this.status = status;
    }
    public Integer getPosicaoPerpendicular() {
        return posicaoPerpendicular;
    }
    public void setPosicaoPerpendicular(Integer posicaoPerpendicular) {
        this.posicaoPerpendicular = posicaoPerpendicular;
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
}
