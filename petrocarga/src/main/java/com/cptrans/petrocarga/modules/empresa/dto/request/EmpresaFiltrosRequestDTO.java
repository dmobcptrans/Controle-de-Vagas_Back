package com.cptrans.petrocarga.modules.empresa.dto.request;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class EmpresaFiltrosRequestDTO {
    private UUID empresaId;
    private UUID usuarioId;
    private String cnpj;
    private String razaoSocial;
    private String nome;
}