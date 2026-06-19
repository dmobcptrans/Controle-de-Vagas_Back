package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response;


import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaSimplificadoResponseDTO;
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
    private MotoristaSimplificadoResponseDTO motorista;
    private EmpresaSimplificadoResponseDTO empresa;
}
