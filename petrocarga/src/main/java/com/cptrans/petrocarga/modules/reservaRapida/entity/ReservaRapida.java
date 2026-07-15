package com.cptrans.petrocarga.modules.reservaRapida.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
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
@Table(name = "reserva_rapida")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
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
    private OffsetDateTime criadoEm = DateUtils.agora();

    @Column(name = "cidade_origem")
    private String cidadeOrigem;

    @Column(name = "entrada_cidade", nullable = true)
    private String entradaCidade;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusReservaEnum status = StatusReservaEnum.ATIVA;

    @Column(name = "posicao_perpendicular")
    private Integer posicaoPerpendicular;

    public ReservaRapida(Vaga vaga,TipoVeiculoEnum tipoVeiculo, String placaVeiculo, OffsetDateTime inicio, OffsetDateTime fim, Integer posicaoPerpendicular, String cidadeOrigem, String entradaCidade) {
        this.vaga = vaga;
        this.tipoVeiculo = tipoVeiculo;
        this.placa = placaVeiculo;
        this.inicio = inicio;
        this.fim = fim;
        this.posicaoPerpendicular = posicaoPerpendicular;
        this.cidadeOrigem = cidadeOrigem;
        this.entradaCidade = entradaCidade;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public void setAgente(Agente agente) {
        this.agente = agente;
    }

    public void setTipoVeiculo(TipoVeiculoEnum tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public void setInicio(OffsetDateTime inicio) {
        this.inicio = inicio;
    }

    public void setFim(OffsetDateTime fim) {
        this.fim = fim;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setCidadeOrigem(String cidadeOrigem) {
        this.cidadeOrigem = cidadeOrigem;
    }

    public void setEntradaCidade(String entradaCidade) {
        this.entradaCidade = entradaCidade;
    }

    public void setStatus(StatusReservaEnum status) {
        this.status = status;
    }

    public void setPosicaoPerpendicular(Integer posicaoPerpendicular) {
        this.posicaoPerpendicular = posicaoPerpendicular;
    }

}