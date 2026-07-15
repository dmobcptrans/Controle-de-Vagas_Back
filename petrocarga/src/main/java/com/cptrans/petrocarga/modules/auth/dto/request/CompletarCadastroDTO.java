package com.cptrans.petrocarga.modules.auth.dto.request;

import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.enums.TipoCnhEnum;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CompletarCadastroDTO {
    @NotNull(message = "CPF é obrigatório!")
    @NotBlank(message = "CPF não pode estar em branco.")
    @CPF(message = "Informe um cpf válido")
    private String cpf;

    @Size(min = 10, max = 11)
    private String telefone;

    @NotNull(message = "Aceitar Termos é obrigatório")
    private Boolean aceitarTermos;
    
    @NotNull(message = "O campo 'tipoCnh' é obrigatório.")
    private TipoCnhEnum tipoCnh;

    @NotNull(message = "O campo 'numeroCnh' é obrigatório.")
    @Size(min = 9, max = 11, message = "Número da CNH deve ter entre 9 e 11 caracteres.")
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