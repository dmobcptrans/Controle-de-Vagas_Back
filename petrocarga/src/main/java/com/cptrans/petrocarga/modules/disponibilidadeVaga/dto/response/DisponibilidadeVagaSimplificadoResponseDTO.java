package com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class DisponibilidadeVagaSimplificadoResponseDTO {
    private UUID id;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
}