package com.cptrans.petrocarga.modules.enderecoVaga.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class EnderecoVagaRequestDTO {
    
    @NotNull(message = "O campo 'logradouro' é obrigatório.")
    @Size(min = 10, max = 255, message="O campo 'logradouro' deve ter entre 10 e 255 caracteres.")
    @Schema(
        description = "Logradouro do endereço da vaga",
        example = "Rua Paulo Barbosa"
    )
    private String logradouro;

    @NotNull(message = "O campo 'bairro' é obrigatório.")
    @Size(min = 3, max = 50, message="O campo 'bairro' deve ter entre 3 e 50 caracteres.")
    @Schema(
        description = "Bairro do endereço da vaga",
        example = "Centro"
    )
    private String bairro;

}