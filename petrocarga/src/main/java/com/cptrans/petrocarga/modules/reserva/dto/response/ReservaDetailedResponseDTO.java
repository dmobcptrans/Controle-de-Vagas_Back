package com.cptrans.petrocarga.modules.reserva.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusReservaEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReservaDetailedResponseDTO {
    private UUID id;
    private UUID vagaId;
    private String numeroEndereco;
    private String referenciaEndereco;
    private String logradouro;
    private String bairro;
    private UUID motoristaId;
    private String motoristaNome;
    private UUID veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private String veiculoMarca;
    private UUID criadoPorId;
    private String criadoPorNome;
    private String cidadeOrigem;
    private String entradaCidade;
    private OffsetDateTime criadoEm;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private StatusReservaEnum status;
}