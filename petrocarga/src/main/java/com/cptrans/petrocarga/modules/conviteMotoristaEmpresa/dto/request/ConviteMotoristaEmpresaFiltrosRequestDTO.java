package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request;

import java.util.List;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusConviteMotoristaEmpresaEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ConviteMotoristaEmpresaFiltrosRequestDTO {
    private UUID conviteId;
    private UUID empresaId;
    private String razaoSocial;
    private String cnpj;
    private List<StatusConviteMotoristaEmpresaEnum> listStatus;
    private UUID motoristaId;
    private String motoristaNome;
    private String motoristaEmail;

    public void setMotoristaEmail(String email){
        this.motoristaEmail = email;
    }
}