package com.cptrans.petrocarga.application.dto;

import java.time.LocalTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.OperacaoVaga;
import com.cptrans.petrocarga.domain.enums.DiaSemanaEnum;

public class OperacaoVagaResponseDTO {

    private UUID id;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;

    public OperacaoVagaResponseDTO() {
    }

    public OperacaoVagaResponseDTO(OperacaoVaga operacaoVaga) {
        this.id = operacaoVaga.getId();
        this.diaSemana = operacaoVaga.getDiaSemana().getDescricao();
        this.horaInicio = operacaoVaga.getHoraInicio();
        this.horaFim = operacaoVaga.getHoraFim();
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemanaEnum diaSemana) {
        this.diaSemana = diaSemana.getDescricao();
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

    public DiaSemanaEnum getDiaSemanaAsEnum() {
       return DiaSemanaEnum.toEnumByDescricao(this.diaSemana);
    }
}