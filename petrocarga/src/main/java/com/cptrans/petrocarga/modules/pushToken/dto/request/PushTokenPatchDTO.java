package com.cptrans.petrocarga.modules.pushToken.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PushTokenPatchDTO (
    @NotNull(message = "O campo 'token' é obrigatório.")
    @NotBlank(message = "O campo 'token' não pode estar em branco.")
    String token,
    @NotNull(message = "O campo 'ativo' é obrigatório.")
    Boolean ativo
){
}