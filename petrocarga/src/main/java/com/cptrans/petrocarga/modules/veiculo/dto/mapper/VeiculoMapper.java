package com.cptrans.petrocarga.modules.veiculo.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;

@Component
public class VeiculoMapper {
    
    public static VeiculoResponseDTO toResponse(Veiculo veiculo) {
        if (veiculo == null) {
            return null;
        }
        return CriptoUtils.decrypt(
            new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getTipo(),
                veiculo.getTipo().getComprimento(),
                veiculo.getUsuario().getId(),
                veiculo.getCpfProprietarioCripto(),
                veiculo.getCnpjProprietario()
            ), 
            veiculo.getUsuario().getPersonalDataKeyVersion()
        );
    }

    public static List<VeiculoResponseDTO> toResponseList(List<Veiculo> veiculos) {
        if (veiculos == null || veiculos.isEmpty()) return List.of();
        return veiculos.stream().map(VeiculoMapper::toResponse).toList(); 
    }
}
