package com.cptrans.petrocarga.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.PlataformaEnum;
import com.cptrans.petrocarga.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "push_token")
public class PushToken {

    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token", nullable = false, unique = true, columnDefinition="TEXT")
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "plataforma", nullable = false, length = 50)
    private PlataformaEnum plataforma;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private final OffsetDateTime CRIADO_EM = OffsetDateTime.now(DateUtils.FUSO_BRASIL);

    public PushToken() {
    }

    public PushToken(UUID usuarioId, String token, PlataformaEnum plataforma) {
        this.usuarioId = usuarioId;
        this.token = token;
        this.plataforma = plataforma;
    }

    public PushToken(String token, PlataformaEnum plataforma) {
        this.token = token;
        this.plataforma = plataforma;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PlataformaEnum getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(PlataformaEnum plataforma) {
        this.plataforma = plataforma;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public OffsetDateTime getCRIADO_EM() {
        return CRIADO_EM;
    }
    
}
