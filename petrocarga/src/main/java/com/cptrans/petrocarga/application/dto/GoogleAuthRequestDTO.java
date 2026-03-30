package com.cptrans.petrocarga.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoogleAuthRequestDTO (
    @NotNull(message = "O campo 'token' é obrigatório.")
    @NotBlank(message = "O campo 'token' não pode estar em branco.")
    String token
){}
