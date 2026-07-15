package com.cptrans.petrocarga.shared.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta padrão de erro da API")
public record SystemResponse(
    @Schema(
        description = "Mensagem de erro",
        example = "Erro interno de servidor"
    )
    String message,

    @Schema(
        description = "Código HTTP",
        example = "500"
    )
    Integer code
) {
}