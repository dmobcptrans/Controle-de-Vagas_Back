package com.cptrans.petrocarga.modules.usuario.dto.response;

import java.util.UUID;

import com.cptrans.petrocarga.shared.utils.StringUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class DadosExtras {
    private String matricula;
    private UUID empresaId;
    private String empresaCnpj;
    private String empresaRazaoSocial;
    private Boolean possuiVeiculoAtivo;

    public DadosExtras(String matricula) {
        this.matricula = matricula;
    }

    public DadosExtras(Boolean possuiVeiculoAtivo) {
        this.possuiVeiculoAtivo = possuiVeiculoAtivo;
    }

    public DadosExtras(UUID empresaId, String empresaCnpj, String empresaRazaoSocial, Boolean possuiVeiculoAtivo) {
        this.empresaId = empresaId;
        this.empresaCnpj = StringUtils.formatarCnpj(empresaCnpj);
        this.empresaRazaoSocial = empresaRazaoSocial;
        this.possuiVeiculoAtivo = possuiVeiculoAtivo;
    }
}