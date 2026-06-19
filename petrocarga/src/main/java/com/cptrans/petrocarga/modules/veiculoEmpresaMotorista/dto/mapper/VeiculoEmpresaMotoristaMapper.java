package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.empresa.dto.mapper.EmpresaMapper;
import com.cptrans.petrocarga.modules.motorista.dto.mapper.MotoristaMapper;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response.VeiculoEmpresaMotoristaResponseDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;

@Component
public class VeiculoEmpresaMotoristaMapper {
    
    public static VeiculoEmpresaMotoristaResponseDTO toResponseDTO(VeiculoEmpresaMotorista veiEmpMotorista) {
        return new VeiculoEmpresaMotoristaResponseDTO(VeiculoMapper.toResponse(veiEmpMotorista.getVeiculo()), MotoristaMapper.toResponseSimplificado(veiEmpMotorista.getMotorista()), EmpresaMapper.toResponseSimplificado(veiEmpMotorista.getMotorista().getEmpresa()));
    }
}
