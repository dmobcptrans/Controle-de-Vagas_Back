package com.cptrans.petrocarga.modules.veiculo.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VeiculoMapper {
    private final CriptoUtils criptoUtils;
    
    public VeiculoResponseDTO toResponse(Veiculo veiculo) {
        if (veiculo == null) {
            return null;
        }
        return criptoUtils.decrypt(
            new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getTipo(),
                veiculo.getTipo().getComprimento(),
                veiculo.getUsuario().getId(),
                veiculo.getCpfProprietarioCripto(),
                veiculo.getCnpjProprietario(),
                veiculo.getAtivo()
            ), 
            veiculo.getUsuario().getPersonalDataKeyVersion()
        );
    }

    public List<VeiculoResponseDTO> toResponseList(List<Veiculo> veiculos) {
        if (veiculos == null || veiculos.isEmpty()) return null;
        return veiculos.stream().map(this::toResponse).toList(); 
    }

    public VeiculoSimplificadoResponseDTO toResponseSimplificado(Veiculo veiculo) {
        if (veiculo == null) return null;
        return new VeiculoSimplificadoResponseDTO(
            veiculo.getId(), 
            veiculo.getMarca(), 
            veiculo.getModelo(), 
            veiculo.getPlaca(),
            veiculo.getTipo(),
            veiculo.getTipo().getComprimento()
        );
    }
}