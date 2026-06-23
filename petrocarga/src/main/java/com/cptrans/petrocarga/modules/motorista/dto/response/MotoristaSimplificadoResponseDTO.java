package com.cptrans.petrocarga.modules.motorista.dto.response;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MotoristaSimplificadoResponseDTO {
    private UUID id;
    private UUID usuarioId;
    private String nome;
    private Boolean ativo;
    private UUID empresaId;
    private String empresaCnpj;
    private String empresaRazaoSocial;
}