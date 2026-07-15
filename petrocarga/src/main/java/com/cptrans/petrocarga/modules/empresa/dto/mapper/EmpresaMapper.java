package com.cptrans.petrocarga.modules.empresa.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class EmpresaMapper {
    private final UsuarioMapper usuarioMapper;

    public EmpresaResponseDTO toResponse(Empresa empresa) {
        if (empresa == null) return null;
        return new EmpresaResponseDTO(
            empresa.getId(),
            usuarioMapper.toResponse(empresa.getUsuario(), empresa.getCnpj())
        );
    }

    public List<EmpresaResponseDTO> toResponseList(List<Empresa> empresas) { 
        if (empresas == null || empresas.isEmpty()) return List.of();
        return empresas.stream().map(this::toResponse).toList(); 
    }

    public EmpresaSimplificadoResponseDTO toResponseSimplificado(Empresa empresa) {
        if (empresa == null) return null;
        return new EmpresaSimplificadoResponseDTO(
            empresa.getId(),
            empresa.getUsuario().getNome(),
            empresa.getCnpj(),
            empresa.getUsuario().getAtivo()
        );
    }

    public List<EmpresaSimplificadoResponseDTO> toResponseSimplificadoList(List<Empresa> empresas) { 
        if (empresas == null || empresas.isEmpty()) return List.of();
        return empresas.stream().map(this::toResponseSimplificado).toList(); 
    }
    
}