package com.cptrans.petrocarga.dto;

import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.models.Usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioRequestDTO {

    @Valid
    @NotNull(message="O campo 'nome' é obrigatório.")
    @Size(min = 2, max = 100, message="Nome deve conter entre 2 e 100 caracteres.")
    private String nome;

    @Valid
    @CPF(message="CPF inválido.")
    private String cpf;

    @Valid
    @NotNull(message="O campo 'telefone' é obrigatório.")
    @Size(min = 10, max = 11, message="Telefone deve conter entre 10 e 11 dígitos.")
    private String telefone;

    @Valid
    @Email(message="Email inválido.")
    private String email;

    @Valid
    @NotNull(message="O campo 'senha' é obrigatório.")
    @Size(min = 6, max = 100, message="Senha deve conter no mínimo 6 caracteres.")
    private String senha;

    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setNome(this.nome);
        usuario.setCpfHash(this.cpf);
        usuario.setTelefone(this.telefone);
        usuario.setEmail(this.email);
        usuario.setSenha(this.senha);
        return usuario;
    }

    // Getters and Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
