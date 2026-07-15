package com.cptrans.petrocarga.modules.reservaRapida.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoVeiculoEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservaRapidaRequestDTO {
    @NotNull(message = "Vaga obrigatória.")
    private UUID vagaId;

    @NotNull(message = "Tipo de veículo obrigatório.")
    private TipoVeiculoEnum tipoVeiculo;

    @NotNull(message = "Placa obrigatória.")
    @Size(min = 7, max = 7, message = "Placa deve ter 7 caracteres.")
    private String placa;

    @NotNull(message = "Data de inicio obrigatória.")
    private OffsetDateTime inicio;

    @NotNull(message = "Data de fim obrigatória.")
    private OffsetDateTime fim;

    private Integer posicaoPerpendicular;

    @NotBlank(message = "Cidade de origem não pode estar vazia.")
    private String cidadeOrigem;

    @NotBlank(message = "Cidade de entrada não pode estar vazia.")
    private String entradaCidade;
}