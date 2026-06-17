package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response;


import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class VeiculoEmpresaMotoristaResponseDTO {
    private VeiculoResponseDTO veiculo;
    private MotoristaResponseDTO motorista;
    private EmpresaResponseDTO empresa;
}
