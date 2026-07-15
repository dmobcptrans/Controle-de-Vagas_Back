package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.empresa.dto.mapper.EmpresaMapper;
import com.cptrans.petrocarga.modules.motorista.dto.mapper.MotoristaMapper;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response.VeiculoEmpresaMotoristaResponseDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VeiculoEmpresaMotoristaMapper {
    private final VeiculoMapper veiculoMapper;
    private final MotoristaMapper motoristaMapper;
    private final EmpresaMapper empresaMapper;
    
    public VeiculoEmpresaMotoristaResponseDTO toResponse(VeiculoEmpresaMotorista veiEmpMotorista) {
        if (veiEmpMotorista == null) return null;
        return new VeiculoEmpresaMotoristaResponseDTO(veiculoMapper.toResponse(veiEmpMotorista.getVeiculo()), motoristaMapper.toResponseSimplificado(veiEmpMotorista.getMotorista()), empresaMapper.toResponseSimplificado(veiEmpMotorista.getMotorista().getEmpresa()));
    }

    public Map<UUID,List<UUID>> getVeiculoMotoristasMap(List<VeiculoEmpresaMotorista> veiEmpMotoristas){
        if (veiEmpMotoristas == null || veiEmpMotoristas.isEmpty()) return null;
        Map<UUID, List<UUID>> response = new HashMap<>();
        veiEmpMotoristas.forEach(vem -> {
            UUID veiculoId = vem.getVeiculo().getId();
            UUID motoristaId = vem.getMotorista().getId();
            if (!response.containsKey(veiculoId)){
                List<UUID> motoristaIds = new ArrayList<>();
                motoristaIds.add(motoristaId);
                response.put(veiculoId, motoristaIds);
            } else {
                response.get(veiculoId).add(motoristaId);
            }
        });
        return response;
    }
}