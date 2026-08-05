package com.cptrans.petrocarga.modules.usuario.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.enums.TipoCnhEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UsuarioPATCHRequestDTO {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @Email(message = "Informe um email válido.")
    private String email;

    @Pattern(
        regexp = "^\\d{10,11}$",
        message = "O telefone deve conter apenas números e ter entre 10 e 11 dígitos"
    )
    private String telefone;

    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{6,}$",
        message = "A senha deve conter no mínimo 6 caracteres, uma letra maiúscula, uma letra minúscula, um número e um caractere especial."
    )
    private String senha;

    @CPF(message = "Informe um CPF válido.")
    private String cpf;

    @Pattern(
        regexp = "^\\d{4,4}$",
        message = "A matrícula deve conter apenas números e ter 4 dígitos"
    )
    private String matricula;

    @CNPJ(message = "Informe um CNPJ válido.")
    private String cnpj;

    @Size(min = 3, max = 100, message = "Razão Social deve ter entre 3 e 100 caracteres.")
    private String razaoSocial;

    private TipoCnhEnum tipoCnh;

    @Pattern(
        regexp = "^\\d{9,11}$",
        message = "O número da CNH deve conter apenas números e ter entre 9 e 11 dígitos"
    )
    private String numeroCnh;

    @Future(message = "A data de validade da CNH não pode estar vencida.")
    private LocalDate dataValidadeCnh;

    private UUID empresaId;
}