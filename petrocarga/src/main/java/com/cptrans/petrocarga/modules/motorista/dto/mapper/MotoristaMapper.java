package com.cptrans.petrocarga.modules.motorista.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResponseDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
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
                    motorista.getCnhHash(),
                    motorista.getDataValidadeCnh(),
                    (motorista.getEmpresa() != null ? motorista.getEmpresa().getId() : null)
                ), motorista.getUsuario().getPersonalDataKeyVersion());
    }
}
