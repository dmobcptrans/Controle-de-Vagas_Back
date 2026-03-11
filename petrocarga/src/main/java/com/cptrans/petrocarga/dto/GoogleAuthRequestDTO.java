package com.cptrans.petrocarga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoogleAuthRequestDTO (
    @NotNull
    @NotBlank
    String token
){
    
}
