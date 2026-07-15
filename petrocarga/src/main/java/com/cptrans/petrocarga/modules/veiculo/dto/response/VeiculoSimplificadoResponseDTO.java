package com.cptrans.petrocarga.modules.veiculo.dto.response;

import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoVeiculoEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class VeiculoSimplificadoResponseDTO {
    private UUID id;
    private String marca;
    private String modelo;
    private String placa;
    private TipoVeiculoEnum tipo;
    private Integer comprimento;
}