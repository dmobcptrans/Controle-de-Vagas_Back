package com.cptrans.petrocarga.modules.motorista.dto.response;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class MotoristaSimplificadoResponseDTO {
    private UUID id;
    private String nome;
    private String telefone;
    private String email;
    private Boolean ativo;
    private UUID empresaId;
    private String empresaCnpj;
    private String empresaRazaoSocial;
}