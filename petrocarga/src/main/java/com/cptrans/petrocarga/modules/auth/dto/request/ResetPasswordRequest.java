package com.cptrans.petrocarga.modules.auth.dto.request;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
    @Email(message = "Informe um email válido.")
    String email,
     
    @CPF(message = "Informe um CPF válido.")
    String cpf,

    @CNPJ(message = "Informe um CNPJ válido.")
    String cnpj,
    
    @NotNull(message = "O campo 'codigo' é obrigatório.")
    @NotBlank(message = "O campo 'codigo' não pode estar vazio.")
    String codigo,

    @NotNull(message = "O campo 'novaSenha' é obrigatório.") 
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{6,}$",
        message = "A senha deve conter no mínimo 6 caracteres, uma letra maiúscula, uma letra minúscula, um número e um caractere especial."
    )
    String novaSenha) {
}