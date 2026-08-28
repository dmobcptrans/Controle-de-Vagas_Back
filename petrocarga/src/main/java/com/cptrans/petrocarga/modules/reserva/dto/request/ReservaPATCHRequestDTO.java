package com.cptrans.petrocarga.modules.reserva.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReservaPATCHRequestDTO {
    private UUID motoristaId;

    private UUID veiculoId;

    @Size(min = 10, max = 100, message = "Cidade Origem deve ter entre 10 e 100 caracteres.")
    private String cidadeOrigem;

    @Future(message = "O campo 'inicio' deve ser uma data futura.")
    private OffsetDateTime inicio;
    
    @Future(message = "O campo 'fim' deve ser uma data futura.")
    private OffsetDateTime fim;
}