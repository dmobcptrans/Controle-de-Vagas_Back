package com.cptrans.petrocarga.models;

import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cptrans.petrocarga.dto.UsuarioResponseDTO;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.utils.UsuarioUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "cpf_hash", nullable = false)
    private String cpfHash;

    @Column(name = "cpf_cripto", nullable = false)
    private String cpfCripto;

    @Column(name = "cpf_key_version", nullable = false)
    private Integer cpfKeyVersion;

    @Column(name = "cpf_last5", nullable = false)
    private String cpfLast5;

    @Column(length = 11)
    private String telefone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissaoEnum permissao;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime criadoEm;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean ativo;

    @Column(name = "desativado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime desativadoEm;

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "verification_code_expires_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(name = "aceitar_termos", nullable = false)
    private Boolean aceitarTermos;

    @Column(name = "aceitou_termos_em", nullable = true)
    private OffsetDateTime aceitouTermosEm;

    @Column(name = "versao_termos", nullable = false)
    private String versaoTermos;

    @Column(name = "google_id", nullable = true, unique = true)
    private String googleId;

    @Column(name="provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private UsuarioProviderEnum provider;

    // Constructors
    public Usuario() {
        this.criadoEm = OffsetDateTime.now();
        this.ativo = false;
        this.aceitarTermos = false;
        this.versaoTermos = UsuarioUtils.VERSAO_ATUAL_TERMOS;
    }

    public Usuario(String nome, String cpf, String telefone, String email, String senha, PermissaoEnum permissao) {
        this.nome = nome;
        this.cpfHash = cpf;
        this.telefone = telefone;
        this.email = email;
        this.senha = senha;
        this.permissao = permissao;
        this.criadoEm = OffsetDateTime.now();
        this.ativo = false;
        this.aceitarTermos=false;
        this.versaoTermos=UsuarioUtils.VERSAO_ATUAL_TERMOS;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
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

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getVerificationCodeExpiresAt() {
        return verificationCodeExpiresAt;
    }

    public void setVerificationCodeExpiresAt(LocalDateTime verificationCodeExpiresAt) {
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    }

    public Boolean isAceitarTermos() {
        return aceitarTermos;
    }

    public void setAceitarTermos(Boolean aceitarTermos) {
        this.aceitarTermos = aceitarTermos;
    }

    public OffsetDateTime getAceitouTermosEm() {
        return aceitouTermosEm;
    }

    public void setAceitouTermosEm(OffsetDateTime aceitouTermosEm) {
        this.aceitouTermosEm = aceitouTermosEm;
    }

    public String getVersaoTermos() {
        return versaoTermos;
    }

    public void setVersaoTermos(String versaoTermos) {
        this.versaoTermos = versaoTermos;
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public void setCpfHash(String cpfHash) {
        this.cpfHash = cpfHash;
    }

    public String getCpfCripto() {
        return cpfCripto;
    }

    public void setCpfCripto(String cpfCripto) {
        this.cpfCripto = cpfCripto;
    }

    public Integer getCpfKeyVersion() {
        return cpfKeyVersion;
    }

    public void setCpfKeyVersion(Integer cpfKeyVersion) {
        this.cpfKeyVersion = cpfKeyVersion;
    }

    public String getCpfLast5() {
        return cpfLast5;
    }

    public void setCpfLast5(String cpfLast5) {
        this.cpfLast5 = cpfLast5;
    }

    public Boolean getAceitarTermos() {
        return aceitarTermos;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public UsuarioProviderEnum getProvider() {
        return provider;
    }

    public void setProvider(UsuarioProviderEnum provider) {
        this.provider = provider;
    }

    public UsuarioResponseDTO toResponseDTO() {
        return new UsuarioResponseDTO(id, nome, cpfLast5, telefone, email, permissao, criadoEm, ativo, desativadoEm);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
          return List.of(new SimpleGrantedAuthority("ROLE_" + permissao.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
       return email;
    }
}
