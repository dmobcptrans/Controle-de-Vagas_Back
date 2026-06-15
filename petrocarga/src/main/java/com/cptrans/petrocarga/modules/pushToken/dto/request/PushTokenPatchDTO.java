package com.cptrans.petrocarga.modules.pushToken.dto.request;

import com.google.firebase.database.annotations.NotNull;

import jakarta.validation.constraints.NotBlank;

public record PushTokenPatchDTO (
    @NotNull
    @NotBlank
    String token,
    @NotNull
    Boolean ativo
){
    
}
