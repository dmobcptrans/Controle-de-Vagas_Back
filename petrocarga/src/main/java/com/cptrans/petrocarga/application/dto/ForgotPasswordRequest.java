package com.cptrans.petrocarga.application.dto;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
    @Valid
    @Email(message = "Informe um email válido.")
    String email,
     
    @Valid
    @CPF(message = "Informe um CPF válido.")
    String cpf) {
}
