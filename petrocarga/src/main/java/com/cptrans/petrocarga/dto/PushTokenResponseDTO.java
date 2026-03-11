package com.cptrans.petrocarga.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.PlataformaEnum;
import com.cptrans.petrocarga.utils.DateUtils;

public class PushTokenResponseDTO {

    private UUID usuarioId;
    private String token;
    private PlataformaEnum plataforma;
    private boolean ativo = true;
    private final OffsetDateTime CRIADO_EM = OffsetDateTime.now(DateUtils.FUSO_BRASIL);

    public PushTokenResponseDTO(){
        
    }

    public PushTokenResponseDTO(UUID usuarUuid, String token, PlataformaEnum plataforma, boolean ativo){
        this.usuarioId = usuarUuid;
        this.token = token;
        this.plataforma = plataforma;
        this.ativo = ativo;
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
