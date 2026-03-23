package com.cptrans.petrocarga.application.dto;

import org.hibernate.validator.constraints.br.CPF;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @Valid
    @Email(message = "Informe um email válido.")
    String email,
     
    @Valid
    @CPF(message = "Informe um CPF válido.")
    String cpf,
    
    @Valid
    @NotNull(message = "O campo 'codigo' é obrigatório.")
    String codigo,

    @Valid
    @NotNull(message = "O campo 'novaSenha' é obrigatório.") 
    @Size(min = 6, max = 100, message = "Senha deve conter no mínimo 6 caracteres.")
    String novaSenha) {
}
