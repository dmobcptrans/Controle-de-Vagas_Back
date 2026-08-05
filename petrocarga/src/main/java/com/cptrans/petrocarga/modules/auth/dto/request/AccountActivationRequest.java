package com.cptrans.petrocarga.modules.auth.dto.request;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record AccountActivationRequest(
    @CNPJ(message = "Informe um CNPJ válido.")
    String cnpj, 
    
    @CPF(message = "Informe um CPF válido.")
    String cpf,
    
    @NotNull(message = "O campo 'codigo' é obrigatório.")
    @NotBlank(message = "O campo 'codigo' não pode estar em branco.")
    String codigo,

    @NotNull(message = "O campo 'aceitarTermos' é obrigatório.")
    @AssertTrue(message = "Você deve aceitar os termos de uso para ativar a conta.")
    Boolean aceitarTermos) {
}