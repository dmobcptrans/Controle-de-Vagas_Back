package com.cptrans.petrocarga.modules.auth.dto.request;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record AccountActivationRequest(
    @Valid
    @CNPJ(message = "Informe um CNPJ válido.")
    String cnpj, 
    
    @Valid
    @CPF(message = "Informe um CPF válido.")
    String cpf,
    
    @Valid
    @NotNull(message = "O campo 'codigo' é obrigatório.")
    @NotBlank(message = "O campo 'codigo' não pode estar em branco.")
    String codigo,

    @Valid
    @NotNull
    Boolean aceitarTermos) {
}
