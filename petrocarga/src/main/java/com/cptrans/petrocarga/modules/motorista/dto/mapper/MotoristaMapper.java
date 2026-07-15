package com.cptrans.petrocarga.modules.motorista.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResponseDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class MotoristaMapper {
    private final CriptoUtils criptoUtils;
    private final UsuarioMapper usuarioMapper;
    
    public MotoristaResponseDTO toResponse(Motorista motorista){
        if (motorista == null) return null;
        Usuario motoristaUsuario = motorista.getUsuario();
        Empresa empresa = motorista.getEmpresa();
        Usuario empresaUsuario = empresa != null ? empresa.getUsuario() : null;
        return criptoUtils.decrypt(
            new MotoristaResponseDTO(
                motorista.getId(),
                (motoristaUsuario != null ? usuarioMapper.toResponse(motoristaUsuario, motorista.getCpfCripto()) : null),
                motorista.getTipoCnh(),
                motorista.getCnhCripto(),
                motorista.getDataValidadeCnh(),
                    (empresa != null ? empresa.getId() : null),
                    (empresa != null ? empresa.getCnpj() : null),
                    (empresaUsuario != null ? empresaUsuario.getNome() : null)
                ), motorista.getUsuario().getPersonalDataKeyVersion());
    }
    
    public List<MotoristaResponseDTO> toResponseList(List<Motorista> motoristas){
        if (motoristas == null || motoristas.isEmpty()) return null;
        return motoristas.stream().map(this::toResponse).toList();
    }

    public MotoristaSimplificadoResponseDTO toResponseSimplificado(Motorista motorista){
        if (motorista == null) return null;
        Usuario motoristaUsuario = motorista.getUsuario();
        String telefoneCriptoMotorista = motoristaUsuario != null ? motoristaUsuario.getTelefoneCripto() : null;
        String emailCriptoMotorista = motoristaUsuario != null ? motoristaUsuario.getEmailCripto() : null;
        Integer keyVersionMotorista = motoristaUsuario != null ? motoristaUsuario.getPersonalDataKeyVersion() : null;
        String telefoneMotorista = telefoneCriptoMotorista != null && keyVersionMotorista != null ? criptoUtils.decrypt(telefoneCriptoMotorista, keyVersionMotorista) : null;
        String emailMotorista = emailCriptoMotorista != null && keyVersionMotorista != null ? criptoUtils.decrypt(emailCriptoMotorista, keyVersionMotorista) : null;
        Empresa empresa = motorista.getEmpresa();
        Usuario empresaUsuario = empresa != null ? empresa.getUsuario() : null;
        return new MotoristaSimplificadoResponseDTO(
                motorista.getId(),
                motorista.getUsuario().getNome(),
                telefoneMotorista,
                emailMotorista,
                motorista.getUsuario().getAtivo(),
                (empresa != null ? empresa.getId() : null),
                (empresa != null ? empresa.getCnpj() : null),
                (empresaUsuario != null ? empresaUsuario.getNome() : null)
            );
    }

    public List<MotoristaSimplificadoResponseDTO> toResponseSimplificadoList(List<Motorista> motoristas){
        if (motoristas == null || motoristas.isEmpty()) return List.of();
        return motoristas.stream().map(this::toResponseSimplificado).toList();
    }
}