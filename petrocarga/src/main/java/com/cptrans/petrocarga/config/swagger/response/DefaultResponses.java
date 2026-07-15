package com.cptrans.petrocarga.config.swagger.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SystemResponse.class)
        )
    ),
    @ApiResponse(responseCode = "403", description = "Acesso negado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SystemResponse.class)
        )
    ),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado ou desativado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SystemResponse.class)
        )
    ),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SystemResponse.class)
        )
    )
})
public @interface DefaultResponses {
}