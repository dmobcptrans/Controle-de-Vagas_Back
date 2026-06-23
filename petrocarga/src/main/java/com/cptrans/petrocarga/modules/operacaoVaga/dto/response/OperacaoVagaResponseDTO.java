package com.cptrans.petrocarga.modules.operacaoVaga.dto.response;

import java.time.LocalTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class OperacaoVagaResponseDTO {
    private UUID id;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
}