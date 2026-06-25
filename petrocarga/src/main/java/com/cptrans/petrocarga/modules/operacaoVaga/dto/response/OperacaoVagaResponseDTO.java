package com.cptrans.petrocarga.modules.operacaoVaga.dto.response;

import java.time.LocalTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.DiaSemanaEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class OperacaoVagaResponseDTO {
    private UUID id;
    private DiaSemanaEnum diaSemanaAsEnum;
    private LocalTime horaInicio;
    private LocalTime horaFim;
}