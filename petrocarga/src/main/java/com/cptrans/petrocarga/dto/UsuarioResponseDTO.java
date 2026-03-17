package com.cptrans.petrocarga.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.models.Usuario;

public class UsuarioResponseDTO {

    private UUID id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private PermissaoEnum permissao;
    private OffsetDateTime criadoEm;
    private Boolean ativo;
    private OffsetDateTime desativadoEm;
    private boolean veiculoCadastrado;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(UUID id, String nome, String cpfLast5, String telefoneLast4, String email, PermissaoEnum permissao,
            OffsetDateTime criadoEm, Boolean ativo, OffsetDateTime desativadoEm) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpfLast5;
        this.telefone = telefoneLast4;
        this.email = email;
        this.permissao = permissao;
        this.criadoEm = criadoEm;
        this.ativo = ativo;
        this.desativadoEm = desativadoEm;
    }

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.cpf = usuario.getCpfLast5();
        this.telefone = usuario.getTelefoneLast4();
        this.email = usuario.getEmail();
        this.permissao = usuario.getPermissao();
        this.criadoEm = usuario.getCriadoEm();
        this.ativo = usuario.isAtivo();
        this.desativadoEm = usuario.getDesativadoEm();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpfLast5) {
        this.cpf = cpfLast5;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PermissaoEnum getPermissao() {
        return permissao;
    }

    public void setPermissao(PermissaoEnum permissao) {
        this.permissao = permissao;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public OffsetDateTime getDesativadoEm() {
        return desativadoEm;
    }

    public void setDesativadoEm(OffsetDateTime desativadoEm) {
        this.desativadoEm = desativadoEm;
    }

    public boolean isVeiculoCadastrado() {
        return veiculoCadastrado;
    }

    public void setVeiculoCadastrado(boolean veiculoCadastrado) {
        this.veiculoCadastrado = veiculoCadastrado;
    }
}
