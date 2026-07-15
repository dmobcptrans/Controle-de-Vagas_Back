package com.cptrans.petrocarga.modules.auth.dto.request;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
    @Valid
    @Email(message = "Informe um email válido.")
    String email,
     
    @Valid
    @CPF(message = "Informe um CPF válido.")
    String cpf,

    @Valid
    @CNPJ(message = "Informe um CNPJ válido.")
    String cnpj) {
}
