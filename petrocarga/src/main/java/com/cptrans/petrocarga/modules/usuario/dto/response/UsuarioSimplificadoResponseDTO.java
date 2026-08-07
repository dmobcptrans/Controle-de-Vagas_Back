package com.cptrans.petrocarga.modules.usuario.dto.response;

import java.util.UUID;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.shared.utils.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class UsuarioSimplificadoResponseDTO {
    private UUID id;
    private String nome;
    private String telefone;
    private String email;
    private String cnpj;
    private PermissaoEnum permissao;
    private Boolean ativo;

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public void formatarDados() {
        telefone = StringUtils.formatarTelefone(telefone);
        cnpj = StringUtils.formatarCnpj(cnpj);
    }
}