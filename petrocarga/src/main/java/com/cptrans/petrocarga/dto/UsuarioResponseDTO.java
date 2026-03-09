package com.cptrans.petrocarga.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.models.Usuario;

public class UsuarioResponseDTO {

    private UUID id;
    private String nome;
    private String cpfLast5;
    private String telefone;
    private String email;
    private PermissaoEnum permissao;
    private OffsetDateTime criadoEm;
    private Boolean ativo;
    private OffsetDateTime desativadoEm;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(UUID id, String nome, String cpfLast5, String telefone, String email, PermissaoEnum permissao,
            OffsetDateTime criadoEm, Boolean ativo, OffsetDateTime desativadoEm) {
        this.id = id;
        this.nome = nome;
        this.cpfLast5 = cpfLast5;
        this.telefone = telefone;
        this.email = email;
        this.permissao = permissao;
        this.criadoEm = criadoEm;
        this.ativo = ativo;
        this.desativadoEm = desativadoEm;
    }

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.cpfLast5 = usuario.getCpfLast5();
        this.telefone = usuario.getTelefone();
        this.email = usuario.getEmail();
        this.permissao = usuario.getPermissao();
        this.criadoEm = usuario.getCriadoEm();
        this.ativo = usuario.getAtivo();
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

    public String getCpfLast5() {
        return cpfLast5;
    }

    public void setCpfLast5(String cpfLast5) {
        this.cpfLast5 = cpfLast5;
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
}
