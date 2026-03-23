package com.cptrans.petrocarga.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.domain.enums.TipoCnhEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UsuarioPATCHRequestDTO {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @Email(message = "Informe um email válido.")
    private String email;

    @Size(min = 10, max = 11, message = "Telefone deve conter entre 10 e 11 dígitos.")
    private String telefone;

    @Size(min = 6, max = 100, message = "Senha deve conter no mínimo 6 caracteres.")
    private String senha;

    @CPF(message = "Informe um CPF válido.")
    private String cpf;

    @Size(min = 6, max = 100, message = "matricula deve conter no mínimo 6 caracteres.")
    private String matricula;

    @CNPJ(message = "Informe um CNPJ válido.")
    private String cnpj;

    @Size(min = 3, max = 100, message = "Razão Social deve ter entre 3 e 100 caracteres.")
    private String razaoSocial;

    @Valid
    private TipoCnhEnum tipoCnh;

    @Size(min = 9, max = 11, message = "CNH deve ter entre 9 e 11 caracteres.")
    private String numeroCnh;

    private LocalDate dataValidadeCnh;

    private UUID empresaId;

    public UsuarioPATCHRequestDTO() {
    }

    public String getNome() {
        return nome;
    }
    public String getEmail() {
        return email;
    }
    public String getTelefone() {
        return telefone;
    }
    public String getSenha() {
        return senha;
    }
    public String getCpf() {
        return cpf;
    }
    public String getMatricula() {
        return matricula;
    }
    public String getCnpj() {
        return cnpj;
    }
    public String getRazaoSocial() {    
        return razaoSocial;
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
    public UUID getEmpresaId() {
        return empresaId;
    }
}
