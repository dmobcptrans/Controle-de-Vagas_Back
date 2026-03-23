package com.cptrans.petrocarga.domain.entities;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.cptrans.petrocarga.domain.enums.TipoNotificacaoEnum;
import com.cptrans.petrocarga.infrastructure.persistance.converter.MetadataJsonConverter;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificacao")
public class Notificacao {
    
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, columnDefinition="TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacaoEnum tipo;

    @Column(name = "lida", nullable = false)
    private boolean lida = false;

    @Column(name = "criada_em", nullable = false)
    private final OffsetDateTime CRIADA_EM = OffsetDateTime.now(DateUtils.FUSO_BRASIL);

    @Convert(converter = MetadataJsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public Notificacao() {
    }

    public Notificacao(UUID usuarioId, String titulo, String mensagem, TipoNotificacaoEnum tipo, Map<String, Object> metadata) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.tipo = tipo;
        this.metadata = metadata;
    }

     public Notificacao(UUID usuarioId, String titulo, String mensagem, TipoNotificacaoEnum tipo) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.tipo = tipo;
    }

    public Notificacao(String titulo, String mensagem, TipoNotificacaoEnum tipo) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.tipo = tipo;
    }

    public Notificacao(String titulo, String mensagem, TipoNotificacaoEnum tipo, Map<String, Object> metadata) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.tipo = tipo;
        this.metadata = metadata;
    }

    public void marcarComoLida() {
        this.lida = true;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public TipoNotificacaoEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacaoEnum tipo) {
        this.tipo = tipo;
    }

    public boolean isLida() {
        return lida;
    }

    public OffsetDateTime getCriadaEm() {
        return CRIADA_EM;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
