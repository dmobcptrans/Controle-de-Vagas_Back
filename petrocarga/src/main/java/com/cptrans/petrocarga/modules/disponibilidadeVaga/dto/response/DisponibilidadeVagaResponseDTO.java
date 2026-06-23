package com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class DisponibilidadeVagaResponseDTO {
    private UUID id;
    private UUID vagaId;
    private EnderecoVagaResponseDTO endereco;
    private String referenciaEndereco;
    private String numeroEndereco;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private OffsetDateTime criadoEm;
    private UUID criadoPorId;
}