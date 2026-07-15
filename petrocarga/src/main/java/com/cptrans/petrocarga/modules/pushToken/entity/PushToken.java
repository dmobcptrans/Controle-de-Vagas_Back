package com.cptrans.petrocarga.modules.pushToken.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.PlataformaEnum;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "push_token")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
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
    private final OffsetDateTime criadoEm = DateUtils.agora();

    public PushToken(UUID usuarioId, String token, PlataformaEnum plataforma) {
        this.usuarioId = usuarioId;
        this.token = token;
        this.plataforma = plataforma;
    }

    public PushToken(String token, PlataformaEnum plataforma) {
        this.token = token;
        this.plataforma = plataforma;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setPlataforma(PlataformaEnum plataforma) {
        this.plataforma = plataforma;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}