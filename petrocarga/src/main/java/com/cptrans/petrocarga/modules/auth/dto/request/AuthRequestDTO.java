package com.cptrans.petrocarga.modules.auth.dto.request;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class AuthRequestDTO {
    
    @Email(message = "Informe um email válido.")
    public String email;

    @CPF(message = "Informe um CPF válido.")
    public String cpf;

    @CNPJ(message = "Informe um CNPJ válido.")
    public String cnpj;

    @NotNull(message = "O campo 'senha' é obrigatório.")
    public String senha;

}