package com.cptrans.petrocarga.modules.usuario.dto.request;

import org.hibernate.validator.constraints.br.CPF;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class UsuarioRequestDTO {

    @NotNull(message="O campo 'nome' é obrigatório.")
    @Size(min = 2, max = 100, message="Nome deve conter entre 2 e 100 caracteres.")
    private String nome;

    @NotNull(message="O campo 'cpf' é obrigatório.")
    @CPF(message="CPF inválido.")
    private String cpf;

    @NotNull(message="O campo 'telefone' é obrigatório.")
    @Size(min = 10, max = 11, message="Telefone deve conter entre 10 e 11 dígitos.")
    private String telefone;

    @NotNull(message="O campo 'email' é obrigatório.")
    @Email(message="Email inválido.")
    private String email;

    @NotNull(message="O campo 'senha' é obrigatório.")
    @Size(min = 6, max = 100, message="Senha deve conter no mínimo 6 caracteres.")
    private String senha;
}