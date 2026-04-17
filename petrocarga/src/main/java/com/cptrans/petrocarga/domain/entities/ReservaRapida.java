package com.cptrans.petrocarga.domain.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.application.dto.ReservaDTO;
import com.cptrans.petrocarga.application.dto.ReservaRapidaResponseDTO;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVeiculoEnum;

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
@Table(name = "reserva_rapida")
public class ReservaRapida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id", nullable = false)
    private Agente agente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_veiculo")
    private TipoVeiculoEnum tipoVeiculo;

    @Column(length = 7, nullable = false)
    private String placa;

    @Column(nullable = false)
    private OffsetDateTime inicio;

    @Column(nullable = false)
    private OffsetDateTime fim;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime criadoEm;

    @Column(name = "cidade_origem")
    private String cidadeOrigem;

    @Column(name = "entrada_cidade", nullable = true)
    private String entradaCidade;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusReservaEnum status;

    @Column(name = "posicao_perpendicular")
    private Integer posicaoPerpendicular;

    // Constructors
    public ReservaRapida() {
        this.criadoEm = OffsetDateTime.now();
        this.status = StatusReservaEnum.ATIVA;
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

    public Agente getAgente() {
        return agente;
    }

    public void setAgente(Agente agente) {
        this.agente = agente;
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

    public ReservaRapidaResponseDTO toResponse() {
        return new ReservaRapidaResponseDTO(this);
    }

    public ReservaDTO toReservaDTO() {
        return new ReservaDTO(this.id, this.vaga.getId(), this.vaga.getNumeroEndereco(), this.vaga.getReferenciaEndereco(), this.vaga.getEndereco().toResponseDTO(), this.inicio, this.fim, this.tipoVeiculo.getComprimento(), this.placa, this.status, this.agente.getUsuario(), this.criadoEm, this.posicaoPerpendicular, this.cidadeOrigem, this.entradaCidade);
    }
}
