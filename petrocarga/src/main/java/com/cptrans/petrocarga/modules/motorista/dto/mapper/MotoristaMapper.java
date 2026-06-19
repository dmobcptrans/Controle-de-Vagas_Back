package com.cptrans.petrocarga.modules.motorista.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResponseDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;


@Component
public class MotoristaMapper {

    
    public static MotoristaResponseDTO toResponse(Motorista motorista){
        if (motorista == null) return null;
        return CriptoUtils.decrypt(
            new MotoristaResponseDTO(
                motorista.getId(),
                (motorista.getUsuario() != null ? UsuarioMapper.toResponse(motorista.getUsuario()) : null),
                motorista.getTipoCnh(),
                motorista.getCnhCripto(),
                motorista.getDataValidadeCnh(),
                    (motorista.getEmpresa() != null ? motorista.getEmpresa().getId() : null),
                    (motorista.getEmpresa() != null ? motorista.getEmpresa().getCnpj() : null),
                    (motorista.getEmpresa() != null ? motorista.getEmpresa().getRazaoSocial() : null),
                    resolveVeiculosEmpresa(motorista.getVeiculosEmpresa())
                ), motorista.getUsuario().getPersonalDataKeyVersion());
    }
    
    public static List<MotoristaResponseDTO> toResponseList(List<Motorista> motoristas){
        if (motoristas == null || motoristas.isEmpty()) return List.of();
        return motoristas.stream().map(MotoristaMapper::toResponse).toList();
    }

    public static MotoristaSimplificadoResponseDTO toResponseSimplificado(Motorista motorista){
        if (motorista == null) return null;
        return new MotoristaSimplificadoResponseDTO(
            motorista.getId(),
            motorista.getUsuario().getId(),
            motorista.getUsuario().getNome(),
            motorista.getUsuario().isAtivo(),
            (motorista.getEmpresa() != null ? motorista.getEmpresa().getId() : null),
            (motorista.getEmpresa() != null ? motorista.getEmpresa().getCnpj() : null),
            (motorista.getEmpresa() != null ? motorista.getEmpresa().getRazaoSocial() : null)
        );
    }

    public static List<MotoristaSimplificadoResponseDTO> toResponseSimplificadoList(List<Motorista> motoristas){
        if (motoristas == null || motoristas.isEmpty()) return List.of();
        return motoristas.stream().map(MotoristaMapper::toResponseSimplificado).toList();
    }

    private static List<VeiculoResponseDTO> resolveVeiculosEmpresa(List<VeiculoEmpresaMotorista> veiculosEmpresa) {
        if (veiculosEmpresa == null || veiculosEmpresa.isEmpty()) return List.of();
        return veiculosEmpresa.stream().map(vem -> {
            Veiculo veiculo = vem.getVeiculo();
            return VeiculoMapper.toResponse(veiculo);
        }).toList();
    }
}
