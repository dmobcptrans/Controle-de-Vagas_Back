package com.cptrans.petrocarga.dto;

import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.enums.TipoCnhEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CompletarCadastroDTO {
    @Valid
    @NotNull(message = "CPF é obrigatório!")
    @NotBlank(message = "CPF não pode estar em branco.")
    @CPF(message = "Informe um cpf válido")
    private String cpf;

    @Valid
    @Size(min = 10, max = 11)
    private String telefone;

    @Valid
    @NotNull(message = "Aceitar Termos é obrigatório")
    private Boolean aceitarTermos;
   
    @NotNull(message = "O campo 'tipoCnh' é obrigatório.")
    @Valid
    private TipoCnhEnum tipoCnh;

    @NotNull(message = "O campo 'numeroCnh' é obrigatório.")
    @Size(min = 9, max = 11, message = "Número da CNH deve ter entre 9 e 11 caracteres.")
    private String numeroCnh;

    @NotNull(message = "O campo 'dataValidadeCnh' é obrigatório.")
    @Valid
    @Future(message = "Data de validade da CNH deve ser futura.")
    private LocalDate dataValidadeCnh;

    @Valid
    @Size(min = 6, max = 100)
    private String senha;


    public CompletarCadastroDTO(
            @Valid @NotNull(message = "CPF é obrigatório!") @NotBlank(message = "CPF não pode estar em branco.") @CPF(message = "Informe um cpf válido") String cpf,
            @Valid @Size(min = 10, max = 11) String telefone,
            @Valid @NotNull(message = "Aceitar Termos é obrigatório") Boolean aceitarTermos,
            @NotNull(message = "O campo 'tipoCnh' é obrigatório.") @Valid TipoCnhEnum tipoCnh,
            @NotNull(message = "O campo 'numeroCnh' é obrigatório.") @Size(min = 9, max = 11, message = "Número da CNH deve ter entre 9 e 11 caracteres.") String numeroCnh,
            @NotNull(message = "O campo 'dataValidadeCnh' é obrigatório.") @Valid @Future(message = "Data de validade da CNH deve ser futura.") LocalDate dataValidadeCnh) {
        this.cpf = cpf;
        this.telefone = telefone;
        this.aceitarTermos = aceitarTermos;
        this.tipoCnh = tipoCnh;
        this.numeroCnh = numeroCnh;
        this.dataValidadeCnh = dataValidadeCnh;
    }


    public CompletarCadastroDTO(
            @Valid @NotNull(message = "CPF é obrigatório!") @NotBlank(message = "CPF não pode estar em branco.") @CPF(message = "Informe um cpf válido") String cpf,
            @Valid @Size(min = 10, max = 11) String telefone,
            @Valid @NotNull(message = "Aceitar Termos é obrigatório") Boolean aceitarTermos,
            @NotNull(message = "O campo 'tipoCnh' é obrigatório.") @Valid TipoCnhEnum tipoCnh,
            @NotNull(message = "O campo 'numeroCnh' é obrigatório.") @Size(min = 9, max = 11, message = "Número da CNH deve ter entre 9 e 11 caracteres.") String numeroCnh,
            @NotNull(message = "O campo 'dataValidadeCnh' é obrigatório.") @Valid @Future(message = "Data de validade da CNH deve ser futura.") LocalDate dataValidadeCnh,
            @Valid @Size(min = 6, max = 100) String senha) {
        this.cpf = cpf;
        this.telefone = telefone;
        this.aceitarTermos = aceitarTermos;
        this.tipoCnh = tipoCnh;
        this.numeroCnh = numeroCnh;
        this.dataValidadeCnh = dataValidadeCnh;
        this.senha = senha;
    }


    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public Boolean getAceitarTermos() {
        return aceitarTermos;
    }

    public String getSenha(){
        return senha;
    }

    public TipoCnhEnum getTipoCnh() {
        return tipoCnh;
    }


    public String getNumeroCnh() {
        return numeroCnh;
    }


    public LocalDate getDataValidadeCnh() {
        return dataValidadeCnh;
    }

}
