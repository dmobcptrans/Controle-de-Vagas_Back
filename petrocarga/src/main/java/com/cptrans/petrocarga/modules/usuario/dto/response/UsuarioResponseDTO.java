package com.cptrans.petrocarga.modules.usuario.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.PermissaoEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class UsuarioResponseDTO {
    private UUID id;
    private String nome;
    private String telefone;
    private String email;
    private String cpf;
    private String cnpj;
    private PermissaoEnum permissao;
    private OffsetDateTime criadoEm;
    private Boolean ativo;
    private OffsetDateTime desativadoEm;
    private Boolean possuiVeiculoAtivo;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setPossuiVeiculoAtivo(Boolean possuiVeiculoAtivo) {
        this.possuiVeiculoAtivo = possuiVeiculoAtivo;
    }
    
}