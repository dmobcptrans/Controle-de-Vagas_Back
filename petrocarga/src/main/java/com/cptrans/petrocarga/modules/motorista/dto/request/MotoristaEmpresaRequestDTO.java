package com.cptrans.petrocarga.modules.motorista.dto.request;

import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.enums.TipoCnhEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MotoristaEmpresaRequestDTO {
    @NotNull(message="O campo 'nome' é obrigatório.")
    @Size(min = 2, max = 100, message="Nome deve conter entre 2 e 100 caracteres.")
    private String nome;

    @NotNull(message="O campo 'telefone' é obrigatório.")
    @Pattern(
        regexp = "^\\d{10,11}$",
        message = "O telefone deve conter apenas números e ter entre 10 e 11 dígitos"
    )
    private String telefone;

    @NotNull(message="O campo 'email' é obrigatório.")
    @Email(message="Email inválido.")
    private String email;

    @NotNull(message="O campo 'cpf' é obrigatório.")
    @CPF(message="CPF inválido.")
    private String cpf;

    @NotNull(message="O campo 'numeroCnh' é obrigatório.")
    @Pattern(
        regexp = "^\\d{9,11}$",
        message = "Número da CNH deve conter apenas números e ter entre 9 e 11 dígitos"
    )
    private String numeroCnh;

    @NotNull(message="O campo 'tipoCnh' é obrigatório.")
    private TipoCnhEnum tipoCnh;

    @NotNull(message="O campo 'dataValidadeCnh' é obrigatório.")
    @Future(message = "Data de validade da CNH está vencida ou vence hoje.")
    private LocalDate dataValidadeCnh;
}