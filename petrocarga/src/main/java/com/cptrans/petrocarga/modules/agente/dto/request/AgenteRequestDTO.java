package com.cptrans.petrocarga.modules.agente.dto.request;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AgenteRequestDTO {

    @NotNull(message="Nome obrigatório.")
    @NotBlank(message="Nome não pode ser vazio.")
    private String nome;

    @NotNull(message="CPF obrigatório.")
    @CPF(message="CPF inválido.")
    private String cpf;

    @NotNull(message="Telefone obrigatório.")
    @Size(min = 10, max = 11, message="Telefone deve conter entre 10 e 11 dígitos.")
    private String telefone;

    @NotNull(message="Email obrigatório.")
    @Email(message="Email inválido.")
    private String email;

    @NotNull(message="Matrícula obrigatória.")
    @Size(min = 4, max = 4, message = "Matrícula deve conter 4 dígitos.")
    private String matricula;

}