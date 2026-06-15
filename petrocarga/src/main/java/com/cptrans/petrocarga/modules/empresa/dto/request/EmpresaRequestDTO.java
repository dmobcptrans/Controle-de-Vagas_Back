package com.cptrans.petrocarga.modules.empresa.dto.request;


import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class EmpresaRequestDTO {

    @NotNull(message = "O campo 'nome' é obrigatório.")
    @NotBlank(message = "O campo 'nome' não pode estar vazio.")
    @Size(min = 3, max = 100, message = "O campo 'nome' deve ter entre 3 e 100 caracteres.")
    private String nome;

    @NotNull(message = "O campo 'email' é obrigatório.")
    @Email(message = "Informe um email válido.")
    private String email;

    @NotNull(message = "O campo 'senha' é obrigatório.")
    @NotBlank(message = "O campo 'senha' não pode estar vazio.")
    @Size(min = 6, max = 100, message = "O campo 'senha' deve ter entre 6 e 100 caracteres.")
    private String senha;

    @NotNull(message = "O campo 'telefone' é obrigatório.")
    @Size(min = 10, max = 11, message = "O campo 'telefone' deve ter 10 ou 11 caracteres (fixo ou celular).")
    private String telefone;

    @NotNull(message = "O campo 'cpf' é obrigatório.")
    @CPF(message = "Informe um CPF válido.")
    private String cpf;

    @NotNull(message = "O campo 'cnpj' é obrigatório.")
    @CNPJ(message = "Informe um CNPJ válido.")
    private String cnpj;

    @NotNull(message = "O campo 'razaoSocial' é obrigatório.")
    @NotBlank(message = "O campo 'razaoSocial' não pode estar vazio.")
    private String razaoSocial;

    @NotNull
    private Boolean aceitouTermos;

}