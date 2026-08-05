package com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DisponibilidadeVagaSimplificadoRequestDTO {
    @NotNull(message = "O campo 'inicio' é obrigatório")
    private OffsetDateTime inicio;

    @NotNull(message = "O campo 'fim' é obrigatório")
    @Future(message = "O horário de fim deve ser posterior ao horário atual.")
    private OffsetDateTime fim;
}