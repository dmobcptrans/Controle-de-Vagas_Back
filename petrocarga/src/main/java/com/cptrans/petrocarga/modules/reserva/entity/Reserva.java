package com.cptrans.petrocarga.modules.reserva.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.shared.utils.DateUtils;

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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserva")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
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
    private OffsetDateTime criadoEm = DateUtils.agora();

    @Column(nullable = false)
    private OffsetDateTime inicio;

    @Column(nullable = false)
    private OffsetDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReservaEnum status = StatusReservaEnum.RESERVADA;

    @Column(name = "checked_in", nullable = false)
    private Boolean checkedIn = false;

    @Column(name = "check_in_em")
    private OffsetDateTime checkInEm;

    @Column(name = "check_out_em", nullable = false)
    private OffsetDateTime checkOutEm;

    @Column(name = "posicao_perpendicular")
    private Integer posicaoPerpendicular;

    public Reserva(Vaga vaga, Motorista motorista, Veiculo veiculo, Usuario criadoPor, String cidadeOrigem, String entradaCidade, OffsetDateTime inicio, OffsetDateTime fim, Integer posicaoPerpendicular) {
        this.vaga = vaga;
        this.motorista = motorista;
        this.veiculo = veiculo;
        this.criadoPor = criadoPor;
        this.cidadeOrigem = cidadeOrigem;
        this.entradaCidade = entradaCidade;
        this.inicio = inicio;
        this.fim = fim;
        this.posicaoPerpendicular = posicaoPerpendicular;

    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }

    public void setCidadeOrigem(String cidadeOrigem) {
        this.cidadeOrigem = cidadeOrigem;
    }

    public void setEntradaCidade(String entradaCidade) {
        this.entradaCidade = entradaCidade;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setInicio(OffsetDateTime inicio) {
        this.inicio = inicio;
    }

    public void setFim(OffsetDateTime fim) {
        this.fim = fim;
    }

    public void setStatus(StatusReservaEnum status) {
        this.status = status;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public void setCheckInEm(OffsetDateTime checkInEm) {
        this.checkInEm = checkInEm;
    }

    public void setCheckOutEm(OffsetDateTime checkOutEm) {
        this.checkOutEm = checkOutEm;
    }

    public void setPosicaoPerpendicular(Integer posicaoPerpendicular) {
        this.posicaoPerpendicular = posicaoPerpendicular;
    }
}