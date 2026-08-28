package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.mapper;


import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.response.ConviteMotoristaEmpresaResponseDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.entity.ConviteMotoristaEmpresa;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.StringUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConviteMotoristaEmpresaMapper {
    private final CriptoUtils criptoUtils;
    
    public ConviteMotoristaEmpresaResponseDTO toResponse(ConviteMotoristaEmpresa convite, boolean privado) {
        if (convite == null) return null;
        Motorista motorista = convite.getMotorista();
        Usuario usuarioMotorista = motorista != null ? motorista.getUsuario() : null;
        Empresa empresa = convite.getEmpresa();
        Usuario usuarioEmpresa = empresa != null ? empresa.getUsuario() : null;
        String email = StringUtils.mascararEmail(criptoUtils.decrypt(convite.getMotoristaEmailCripto(), convite.getCriptoVersion()));
        return new ConviteMotoristaEmpresaResponseDTO(
            privado ? convite.getId() : null,
            usuarioEmpresa != null ? usuarioEmpresa.getNome() : null,
            usuarioMotorista != null ? usuarioMotorista.getNome() : null,
            email,
            motorista != null ? true : false,
            convite.getStatus(),
            DateUtils.formatarData(convite.getCriadoEm()), 
            DateUtils.formatarData(convite.getRespondidoEm())
        );
    }
}
