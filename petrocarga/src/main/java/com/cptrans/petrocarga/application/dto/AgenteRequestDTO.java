package com.cptrans.petrocarga.application.dto;

import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.domain.entities.Agente;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AgenteRequestDTO {

    @NotBlank(message="Nome não pode ser vazio.")
    private String nome;

    @NotBlank
    @Valid
    @CPF(message="CPF inválido.")
    private String cpf;

    @Size(min = 10, max = 11, message="Telefone deve conter entre 10 e 11 dígitos.")
    private String telefone;

    @NotBlank
    @Valid
    @Email(message="Email inválido.")
    private String email;

    @Size(min = 4, max = 4, message = "Matrícula deve conter 4 dígitos.")
    private String matricula;

    public Agente toEntity() {
        String primeiraSenha = this.cpf;
        Usuario usuario = new Usuario(this.nome, this.cpf, this.telefone, this.email, primeiraSenha, PermissaoEnum.AGENTE);
        Agente agente = new Agente();
        agente.setUsuario(usuario);
        agente.setMatricula(this.matricula);
        return agente;
    }

    // Getters and Setters
    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }
    public String getTelefone() {
        return telefone;
    }
    public String getEmail() {
        return email;
    }
    public String getMatricula() {
        return matricula;
    }
    
}
