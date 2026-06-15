package com.cptrans.petrocarga.modules.denuncia.dto.request;

import com.cptrans.petrocarga.enums.StatusDenunciaEnum;

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
