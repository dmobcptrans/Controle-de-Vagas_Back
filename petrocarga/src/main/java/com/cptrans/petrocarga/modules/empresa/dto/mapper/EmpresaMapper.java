package com.cptrans.petrocarga.modules.empresa.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;


@Component
public class EmpresaMapper {
    
    public static EmpresaResponseDTO toResponse(Empresa empresa) {
        return new EmpresaResponseDTO(
            empresa.getId(),
            CriptoUtils.decrypt(UsuarioMapper.toResponse(empresa.getUsuario()),
            empresa.getUsuario().getPersonalDataKeyVersion()),
            empresa.getCnpj(),
            empresa.getRazaoSocial()
        );
    }
    
}