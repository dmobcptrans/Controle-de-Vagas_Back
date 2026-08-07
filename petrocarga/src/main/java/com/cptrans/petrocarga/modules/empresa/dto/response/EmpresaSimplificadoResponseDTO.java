package com.cptrans.petrocarga.modules.empresa.dto.response;

import java.util.UUID;

import com.cptrans.petrocarga.shared.utils.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class EmpresaSimplificadoResponseDTO {
    private UUID id;
    private String nome;
    private String cnpj;
    private Boolean ativo;

    public void formatarDados(){
        this.cnpj = StringUtils.formatarCnpj(cnpj);
    }
}