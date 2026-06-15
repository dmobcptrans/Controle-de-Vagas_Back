package com.cptrans.petrocarga.modules.pushToken.dto.request;

import com.cptrans.petrocarga.enums.PlataformaEnum;
import com.cptrans.petrocarga.modules.pushToken.entity.PushToken;

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
