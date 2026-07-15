package com.cptrans.petrocarga.modules.usuario.entity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class Usuario implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "telefone_hash", nullable = true)
    private String telefoneHash;

    @Column(name = "telefone_cripto", nullable = true)
    private String telefoneCripto;

    @Column(name = "telefone_last4", nullable = true)
    private String telefoneLast4;

    @Column(name = "email_hash", nullable = false, unique = true)
    private String emailHash;

    @Column(name = "email_cripto", nullable = false)
    private String emailCripto;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissaoEnum permissao;

    @Column(name = "criado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private final OffsetDateTime criadoEm = DateUtils.agora();

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean ativo = false;

    @Column(name = "desativado_em", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime desativadoEm;

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "verification_code_expires_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(name = "aceitar_termos", nullable = false)
    private Boolean aceitarTermos = false;

    @Column(name = "aceitou_termos_em", nullable = true)
    private OffsetDateTime aceitouTermosEm;

    @Column(name = "versao_termos", nullable = false)
    private String versaoTermos = UsuarioUtils.VERSAO_ATUAL_TERMOS;

    @Column(name = "google_id", nullable = true, unique = true)
    private String googleId;

    @Column(name="provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private UsuarioProviderEnum provider = UsuarioProviderEnum.LOCAL;

    @Column(name = "personal_data_key_version", nullable = false)
    private Integer personalDataKeyVersion;

    @OneToMany(mappedBy = "usuario")
    private List<Veiculo> veiculos;


    public Usuario(String nome, String telefone, String email, String senha, PermissaoEnum permissao) {
        this.nome = nome;
        this.telefoneHash = telefone;
        this.emailHash = email;
        this.senha = senha;
        this.permissao = permissao;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTelefoneHash(String telefoneHash) {
        this.telefoneHash = telefoneHash;
    }

    public void setTelefoneCripto(String telefoneCripto) {
        this.telefoneCripto = telefoneCripto;
    }

    public void setTelefoneLast4(String telefoneLast4) {
        this.telefoneLast4 = telefoneLast4;
    }

    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }

    public void setEmailCripto(String emailCripto) {
        this.emailCripto = emailCripto;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setPermissao(PermissaoEnum permissao) {
        this.permissao = permissao;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public void setDesativadoEm(OffsetDateTime desativadoEm) {
        this.desativadoEm = desativadoEm;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public void setVerificationCodeExpiresAt(LocalDateTime verificationCodeExpiresAt) {
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    }

    public void setAceitarTermos(Boolean aceitarTermos) {
        this.aceitarTermos = aceitarTermos;
    }

    public void setAceitouTermosEm(OffsetDateTime aceitouTermosEm) {
        this.aceitouTermosEm = aceitouTermosEm;
    }

    public void setVersaoTermos(String versaoTermos) {
        this.versaoTermos = versaoTermos;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public void setProvider(UsuarioProviderEnum provider) {
        this.provider = provider;
    }

    public void setPersonalDataKeyVersion(Integer personalDataKeyVersion) {
        this.personalDataKeyVersion = personalDataKeyVersion;
    }

    public List<Veiculo> getVeiculosAtivos(){
        if (this.veiculos != null && !this.veiculos.isEmpty()) {
            return this.veiculos.stream().filter(veiculo -> veiculo.getAtivo()).toList();
        }
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
          return List.of(new SimpleGrantedAuthority(permissao.getRole()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
       return emailCripto;
    }
}