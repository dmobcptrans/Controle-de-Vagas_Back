package com.cptrans.petrocarga.modules.auth.dto.request;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;

public record ResendCodeRequest(
    @Email(message = "Informe um email válido.")
    String email,
    
    @CPF(message = "Informe um CPF válido.")
    String cpf,

    @CNPJ(message = "Informe um CNPJ válido.")
    String cnpj
) {
}