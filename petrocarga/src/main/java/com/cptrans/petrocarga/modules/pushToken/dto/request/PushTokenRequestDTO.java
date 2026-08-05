package com.cptrans.petrocarga.modules.pushToken.dto.request;

import com.cptrans.petrocarga.enums.PlataformaEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PushTokenRequestDTO {
    @NotNull(message = "O campo 'token' é obrigatório.")
    @NotBlank(message = "O campo 'token' não pode estar em branco.")
    private String token;

    @NotNull(message = "O campo 'plataforma' é obrigatório.")
    private PlataformaEnum plataforma;
}