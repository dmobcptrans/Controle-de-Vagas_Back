package com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DisponibilidadeVagaRequestDTO {
    @NotNull(message = "O campo 'vagaId' é obrigatório")
    private UUID vagaId;

    @NotNull(message = "O campo 'inicio' é obrigatório")
    private OffsetDateTime inicio;

    @NotNull(message = "O campo 'fim' é obrigatório")
    @Future(message = "O horário de fim deve ser posterior ao horário atual.")
    private OffsetDateTime fim;
}