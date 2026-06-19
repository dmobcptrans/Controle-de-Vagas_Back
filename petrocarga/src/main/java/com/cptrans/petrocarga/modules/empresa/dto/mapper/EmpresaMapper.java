package com.cptrans.petrocarga.modules.empresa.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.motorista.dto.mapper.MotoristaMapper;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;


@Component
public class EmpresaMapper {
    
    public static EmpresaResponseDTO toResponse(Empresa empresa) {
        if (empresa == null) return null;
        return new EmpresaResponseDTO(
            empresa.getId(),
            CriptoUtils.decrypt(UsuarioMapper.toResponse(empresa.getUsuario()),
            empresa.getUsuario().getPersonalDataKeyVersion()),
            empresa.getCnpj(),
            empresa.getRazaoSocial(),
            empresa.getMotoristas() != null && !empresa.getMotoristas().isEmpty() ? MotoristaMapper.toResponseSimplificadoList(empresa.getMotoristas()) : null
        );
    }

    public static List<EmpresaResponseDTO> toResponseList(List<Empresa> empresas) { 
        if (empresas == null || empresas.isEmpty()) return List.of();
        return empresas.stream().map(EmpresaMapper::toResponse).toList(); 
    }

    public static EmpresaSimplificadoResponseDTO toResponseSimplificado(Empresa empresa) {
        if (empresa == null) return null;
        return new EmpresaSimplificadoResponseDTO(
            empresa.getId(),
            empresa.getUsuario().getId(),
            empresa.getUsuario().getNome(),
            empresa.getCnpj(),
            empresa.getRazaoSocial(),
            empresa.getUsuario().isAtivo()
        );
    }

    public static List<EmpresaSimplificadoResponseDTO> toResponseSimplificadoList(List<Empresa> empresas) { 
        if (empresas == null || empresas.isEmpty()) return List.of();
        return empresas.stream().map(EmpresaMapper::toResponseSimplificado).toList(); 
    }
    

}