package com.cptrans.petrocarga.modules.disponibilidadeVaga.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disponibilidade_vaga")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class DisponibilidadeVaga {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @Column(nullable = false)
    private OffsetDateTime inicio;

    @Column(nullable = false)
    private OffsetDateTime fim;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private final OffsetDateTime criadoEm = DateUtils.agora();

    @Column(name = "criado_por_id", nullable = false)
    private UUID criadoPorId;

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public void setInicio(OffsetDateTime inicio) {
        this.inicio = inicio;
    }

    public void setFim(OffsetDateTime fim) {
        this.fim = fim;
    }

    public void setCriadoPorId(UUID criadoPorId) {
        this.criadoPorId = criadoPorId;
    }
}