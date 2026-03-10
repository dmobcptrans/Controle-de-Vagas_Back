package com.cptrans.petrocarga.dto;

import jakarta.validation.constraints.NotNull;

public class PushTokenStatusDTO {

    @NotNull
    private boolean ativo;

    public boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
