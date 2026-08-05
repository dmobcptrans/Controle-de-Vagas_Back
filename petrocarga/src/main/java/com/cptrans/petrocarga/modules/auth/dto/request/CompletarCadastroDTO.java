package com.cptrans.petrocarga.modules.auth.dto.request;

import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.enums.TipoCnhEnum;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CompletarCadastroDTO {
    @NotNull(message = "CPF é obrigatório!")
    @NotBlank(message = "CPF não pode estar em branco.")
    @CPF(message = "Informe um cpf válido")
    private String cpf;

    @NotNull(message = "Telefone é obrigatório!")
    @NotBlank(message = "Telefone não pode estar em branco.")
    @Pattern(
        regexp = "^\\d{10,11}$",
        message = "O telefone deve conter apenas números e ter entre 10 e 11 dígitos"
    )
    private String telefone;

    @NotNull(message = "Aceitar Termos é obrigatório")
    @AssertTrue(message = "Você deve aceitar os termos de uso para ativar a conta.")
    private Boolean aceitarTermos;
    
    @NotNull(message = "O campo 'tipoCnh' é obrigatório.")
    private TipoCnhEnum tipoCnh;

    @NotNull(message = "O campo 'numeroCnh' é obrigatório.")
    @Pattern(
        regexp = "^\\d{9,11}$",
        message = "O número da CNH deve conter apenas números e ter entre 9 e 11 dígitos"
    )
    private String numeroCnh;

    @NotNull(message = "O campo 'dataValidadeCnh' é obrigatório.")
    @Future(message = "Data de validade da CNH deve ser futura.")
    private LocalDate dataValidadeCnh;

    @NotNull(message = "O campo 'senha' é obrigatório.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{6,}$",
        message = "A senha deve conter no mínimo 6 caracteres, uma letra maiúscula, uma letra minúscula, um número e um caractere especial."
    )
    private String senha;
}