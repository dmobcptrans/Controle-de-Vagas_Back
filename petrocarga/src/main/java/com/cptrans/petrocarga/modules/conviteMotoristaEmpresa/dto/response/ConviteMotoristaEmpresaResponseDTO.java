package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.response;

import com.cptrans.petrocarga.enums.StatusConviteMotoristaEmpresaEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ConviteMotoristaEmpresaResponseDTO {
    private String razaoSocial;
    private String motoristaNome;
    private String motoristaEmail;
    private boolean motoristaJaCadastrado;
    private StatusConviteMotoristaEmpresaEnum status;
    private String criadoEm;
    private String respondidoEm;
}