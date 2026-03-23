package com.cptrans.petrocarga.domain.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.application.dto.DenunciaResponseDTO;
import com.cptrans.petrocarga.domain.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.domain.enums.TipoDenunciaEnum;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "denuncia")
public class Denuncia {
    
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition="TEXT")
    private String descricao;

    @OneToOne
    @JoinColumn(name = "criado_por", nullable = false)
    private Usuario criadoPor;

    @ManyToOne
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(nullable = false, columnDefinition="VARCHAR(50) DEFAULT 'ABERTA'")
    @Enumerated(EnumType.STRING)
    private StatusDenunciaEnum status = StatusDenunciaEnum.ABERTA;

    @Column(nullable = false, columnDefinition="VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private TipoDenunciaEnum tipo;

    @Column(name = "resposta", columnDefinition="TEXT", nullable = true)
    private String resposta;
    
    @Column(name = "criado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private final OffsetDateTime criadoEm = OffsetDateTime.now(DateUtils.FUSO_BRASIL);

    @OneToOne
    @JoinColumn(name = "atualizado_por", nullable = true)
    private Usuario atualizadoPor;

    @Column(name = "atualizado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime atualizadoEm;

    @Column(name = "encerrado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime encerradoEm;
   

    public Denuncia() {
    }

    public Denuncia(String descricao, Usuario criadoPor, Vaga vaga, Reserva reserva, StatusDenunciaEnum status, TipoDenunciaEnum tipo) {
        this.descricao = descricao;
        this.criadoPor = criadoPor;
        this.vaga = vaga;
        this.reserva = reserva;
        this.status = status;
        this.tipo = tipo;
    }

    public Denuncia(String descricao, Usuario criadoPor, Vaga vaga, Reserva reserva, TipoDenunciaEnum tipo) {
        this.descricao = descricao;
        this.criadoPor = criadoPor;
        this.vaga = vaga;
        this.reserva = reserva;
        this.tipo = tipo;
    }

    public UUID getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Usuario getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public StatusDenunciaEnum getStatus() {
        return status;
    }

    public void setStatus(StatusDenunciaEnum status) {
        this.status = status;
    }

    public TipoDenunciaEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoDenunciaEnum tipo) {
        this.tipo = tipo;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }
  
    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public Usuario getAtualizadoPor() {
        return atualizadoPor;
    }

    public void setAtualizadoPor(Usuario atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(OffsetDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public OffsetDateTime getEncerradoEm() {
        return encerradoEm;
    }

    public void setEncerradoEm(OffsetDateTime encerradoEm) {
        this.encerradoEm = encerradoEm;
    }

    public DenunciaResponseDTO toResponseDTO(){
        return new DenunciaResponseDTO(this);
    }

}
