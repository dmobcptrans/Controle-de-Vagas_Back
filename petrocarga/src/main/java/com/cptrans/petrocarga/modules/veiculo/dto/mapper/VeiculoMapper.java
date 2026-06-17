package com.cptrans.petrocarga.modules.veiculo.dto.mapper;

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
}
