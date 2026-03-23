package com.cptrans.petrocarga.application.dto;

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
