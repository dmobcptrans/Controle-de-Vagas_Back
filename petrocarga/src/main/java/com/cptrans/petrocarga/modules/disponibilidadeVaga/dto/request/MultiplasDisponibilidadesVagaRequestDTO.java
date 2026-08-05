package com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.request;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MultiplasDisponibilidadesVagaRequestDTO {
    @NotNull(message = "A lista de vaga id deve ser informada")
    @NotEmpty(message = "A lista de vaga id não pode estar vazia")
    private List<UUID> listaVagaId;

    @NotNull(message = "O campo 'inicio' é obrigatório")
    private OffsetDateTime inicio;

    @NotNull(message = "O campo 'fim' é obrigatório")
    @Future(message = "O horário de fim deve ser posterior ao horário atual.")
    private OffsetDateTime fim;

}