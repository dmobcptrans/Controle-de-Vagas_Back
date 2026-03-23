package com.cptrans.petrocarga.application.dto;

import java.time.LocalTime;

import com.cptrans.petrocarga.domain.entities.OperacaoVaga;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.DiaSemanaEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class OperacaoVagaRequestDTO {
    @NotNull(message = "O campo 'codigoDiaSemana' é obrigatório.")
    @Schema(description = "Código do dia da semana (do 1 ao 7)", example = "1")
    private Integer codigoDiaSemana;
    @Schema(description = "Hora de inicio", example = "00:00")
    private LocalTime horaInicio;
    @Schema(description = "Hora de fim", example = "13:00")
    private LocalTime horaFim;

    public OperacaoVaga toEntity(Vaga vaga) {
        OperacaoVaga operacaoVaga = new OperacaoVaga();
        operacaoVaga.setVaga(vaga);
        operacaoVaga.setDiaSemana(DiaSemanaEnum.toEnumByCodigo(this.codigoDiaSemana));
        operacaoVaga.setHoraInicio(this.horaInicio);
        operacaoVaga.setHoraFim(this.horaFim);
        return operacaoVaga;
    }

    // Getters e Setters
    public Integer getCodigoDiaSemana() {
        return codigoDiaSemana;
    }
    public void setCodigoDiaSemana(Integer codigoDiaSemana) {
        this.codigoDiaSemana = codigoDiaSemana;
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
}