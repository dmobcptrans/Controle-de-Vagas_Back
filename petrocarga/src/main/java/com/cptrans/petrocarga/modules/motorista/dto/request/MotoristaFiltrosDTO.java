package com.cptrans.petrocarga.modules.motorista.dto.request;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MotoristaFiltrosDTO {
    private UUID id;
    private String nome;
    private String telefone;
    private String email;
    private String cpf;
    private String cnh;
    private UUID empresaId;
    private String empresaCnpj;
    private String empresaRazaoSocial;
    private Boolean ativo;

    public void setTelefone(String telefone) {
        this.telefone = telefone.trim();
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public void setCnh(String cnh) {
        this.cnh = cnh.trim();
    }

    public void setCpf(String cpf) {
        this.cpf = cpf.trim();
    }
}