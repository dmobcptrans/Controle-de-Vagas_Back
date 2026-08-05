package com.cptrans.petrocarga.modules.operacaoVaga.dto.request;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OperacaoVagaRequestDTO {
    @NotNull(message = "O campo 'codigoDiaSemana' é obrigatório.")
    @Schema(description = "Código do dia da semana (do 1 ao 7)", example = "1")
    private Integer codigoDiaSemana;

    @NotNull(message = "O campo 'horaInicio' é obrigatório.")
    @Schema(description = "Hora de inicio", example = "00:00")
    private LocalTime horaInicio;

    @NotNull(message = "O campo 'horaFim' é obrigatório.")
    @Schema(description = "Hora de fim", example = "13:00")
    private LocalTime horaFim;
}