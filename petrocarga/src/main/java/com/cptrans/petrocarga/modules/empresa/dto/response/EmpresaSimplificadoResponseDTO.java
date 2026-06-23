package com.cptrans.petrocarga.modules.empresa.dto.response;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class EmpresaSimplificadoResponseDTO {
    private UUID id;
    private UUID usuarioid;
    private String nome;
    private String cnpj;
    private String razaoSocial;
    private Boolean ativo;
}