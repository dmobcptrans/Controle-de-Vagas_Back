package com.cptrans.petrocarga.domain.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.application.dto.ReservaDTO;
import com.cptrans.petrocarga.application.dto.ReservaResponseDTO;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "motorista_id", nullable = false)
    private Motorista motorista;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "criado_por", nullable = false)
    private Usuario criadoPor;

    @Column(name = "cidade_origem", length = 100)
    private String cidadeOrigem;

    @Column(name = "entrada_cidade", length = 100, nullable = true)
    private String entradaCidade;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime criadoEm;

    @Column(nullable = false)
    private OffsetDateTime inicio;

    @Column(nullable = false)
    private OffsetDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReservaEnum status;

    @Column(name = "checked_in", nullable = false)
    private Boolean checkedIn = false;

    @Column(name = "check_in_em")
    private OffsetDateTime checkInEm;

    @Column(name = "check_out_em", nullable = false)
    private OffsetDateTime checkOutEm;

    // Constructors
    public Reserva() {
        this.criadoEm = OffsetDateTime.now();
        this.status = StatusReservaEnum.RESERVADA;
        this.checkedIn = false;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Usuario getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
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

    public ReservaResponseDTO toResponseDTO() {
        return new ReservaResponseDTO(this);
    }
    public ReservaDTO toReservaDTO(){
        return new ReservaDTO(this.id, this.cidadeOrigem, this.entradaCidade, this.checkedIn, this.checkInEm, this.checkOutEm, this.vaga, this.inicio, this.fim, this.veiculo, this.status, this.criadoPor, this.criadoEm, this.motorista);
    }
}
