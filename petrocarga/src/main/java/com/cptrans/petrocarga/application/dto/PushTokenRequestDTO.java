package com.cptrans.petrocarga.application.dto;

import com.cptrans.petrocarga.domain.entities.PushToken;
import com.cptrans.petrocarga.domain.enums.PlataformaEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PushTokenRequestDTO {
    @NotNull
    @NotBlank
    private String token;

    @NotNull
    private PlataformaEnum plataforma;

    public String getToken() {
        return token;
    }

    public PlataformaEnum getPlataforma() {
        return plataforma;
    }

    public PushToken toEntity(){
        return new PushToken(this.token, this.plataforma);
    }
}
