package com.cptrans.petrocarga.application.dto;

import com.cptrans.petrocarga.domain.enums.StatusDenunciaEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FinalizarDenunciaRequestDTO {
    @NotNull
    @NotBlank
    private String resposta;

    @NotNull
    private StatusDenunciaEnum status;

    public String getResposta() {
        return resposta;
    }

    public StatusDenunciaEnum getStatus() {
        return status;
    }
}
