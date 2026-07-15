package com.cptrans.petrocarga.modules.denuncia.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.TipoDenunciaEnum;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "denuncia")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
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
    private final OffsetDateTime criadoEm = DateUtils.agora();

    @OneToOne
    @JoinColumn(name = "atualizado_por", nullable = true)
    private Usuario atualizadoPor;

    @Column(name = "atualizado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime atualizadoEm;

    @Column(name = "encerrado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime encerradoEm;

    public Denuncia(String descricao, Usuario criadoPor, Reserva reserva, StatusDenunciaEnum status, TipoDenunciaEnum tipo) {
        this.descricao = descricao;
        this.criadoPor = criadoPor;
        this.vaga = reserva.getVaga();
        this.reserva = reserva;
        this.status = status;
        this.tipo = tipo;
    }

    public Denuncia(String descricao, Usuario criadoPor, Reserva reserva, TipoDenunciaEnum tipo) {
        this.descricao = descricao;
        this.criadoPor = criadoPor;
        this.vaga = reserva.getVaga();
        this.reserva = reserva;
        this.tipo = tipo;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public void setStatus(StatusDenunciaEnum status) {
        this.status = status;
    }

    public void setTipo(TipoDenunciaEnum tipo) {
        this.tipo = tipo;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }
  
    public void setAtualizadoPor(Usuario atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }

    public void setAtualizadoEm(OffsetDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public void setEncerradoEm(OffsetDateTime encerradoEm) {
        this.encerradoEm = encerradoEm;
    }
}