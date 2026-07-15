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
    @ApiResponse(responseCode = "201", description = "Recurso alterado com sucesso"),
    @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SystemResponse.class)
        )
    ),
    @ApiResponse(responseCode = "409", description = "Conflito de dados",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SystemResponse.class)
        )
    )
})
public @interface PatchResponses {
}