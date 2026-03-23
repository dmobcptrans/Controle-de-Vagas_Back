package com.cptrans.petrocarga.domain.entities;

import java.time.LocalTime;
import java.util.UUID;

import com.cptrans.petrocarga.application.dto.OperacaoVagaResponseDTO;
import com.cptrans.petrocarga.domain.enums.DiaSemanaEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "operacao_vaga", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"vaga_id", "dia_semana"})
})
public class OperacaoVaga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    @JsonBackReference
    private Vaga vaga;

    @Column(name = "dia_semana", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiaSemanaEnum diaSemana;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    public OperacaoVaga() {
    }

    public OperacaoVaga(Vaga vaga, DiaSemanaEnum diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.vaga = vaga;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    // Getters e Setters

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

    public DiaSemanaEnum getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemanaEnum diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperacaoVaga that = (OperacaoVaga) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        // Garante que o hashCode seja consistente para entidades antes e depois da persistência
        return getClass().hashCode();
    }
    
    public OperacaoVagaResponseDTO toResponseDTO() {
        OperacaoVagaResponseDTO dto = new OperacaoVagaResponseDTO();
        dto.setId(this.id);
        dto.setDiaSemana(this.diaSemana);
        dto.setHoraInicio(this.horaInicio);
        dto.setHoraFim(this.horaFim);
        return dto;
    }
}