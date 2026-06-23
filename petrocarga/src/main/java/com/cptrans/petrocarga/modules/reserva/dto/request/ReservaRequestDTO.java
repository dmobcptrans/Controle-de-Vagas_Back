package com.cptrans.petrocarga.modules.reserva.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReservaRequestDTO {
    @NotNull(message = "O campo 'vagaId' é obrigatório.")
    private UUID vagaId;
    @NotNull(message = "O campo 'motoristaId' é obrigatório.")
    private UUID motoristaId;
    @NotNull(message = "O campo 'veiculoId' é obrigatório.")
    private UUID veiculoId;
    @NotNull(message = "O campo 'cidadeOrigem' é obrigatório.")
    @NotBlank(message = "O campo 'cidadeOrigem' não pode estar em branco.")
    private String cidadeOrigem;
    @Size(min = 3, max = 100, message = "O campo 'entradaCidade' deve ter entre 3 e 100 caracteres.")
    private String entradaCidade;
    @NotNull(message = "O campo 'inicio' é obrigatório.")
    private OffsetDateTime inicio;
    @NotNull(message = "O campo 'fim' é obrigatório.")
    private OffsetDateTime fim;
    private Integer posicaoPerpendicular;
}