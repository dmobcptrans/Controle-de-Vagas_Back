package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class VeiculoEmpresaMotoristaSimplicifadoResponseDTO {
    private Map<UUID, List<UUID>> veiculoMotoristas;
}