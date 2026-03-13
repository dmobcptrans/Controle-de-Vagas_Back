package com.cptrans.petrocarga.dto;

import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.models.Usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GestorRequestDTO {
    @NotBlank
    private String nome;

    @Valid
    @Email(message = "Informe um email válido.")
    private String email;

    @Valid
    @CPF(message = "Informe um CPF válido.")
    private String cpf;

    @Size(min = 10, max = 11,message= "Telefone deve conter entre 10 e 11 dígitos.")
    private String telefone;

    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setNome(this.nome);
        usuario.setEmail(this.email);
        usuario.setCpfHash(this.cpf);
        usuario.setSenha(this.cpf);
        usuario.setTelefoneHash(this.telefone);
        usuario.setPermissao(PermissaoEnum.GESTOR);
        return usuario;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail(){
        return email;
    }

    public String getCpf(){
        return cpf;
    }

    public String getTelefone(){
        return telefone;
    }
}
