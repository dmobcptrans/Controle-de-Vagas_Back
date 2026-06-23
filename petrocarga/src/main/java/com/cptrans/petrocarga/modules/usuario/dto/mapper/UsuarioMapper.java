package com.cptrans.petrocarga.modules.usuario.dto.mapper;



import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;


@Component
public class UsuarioMapper {
    public static Usuario toEntity(UsuarioRequestDTO request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome().trim());
        return usuario;
    }    

    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        if (usuario == null) return null;
        return CriptoUtils.decrypt(
            new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpfCripto(),
                usuario.getTelefoneCripto(),
                usuario.getEmailCripto(),
                usuario.getPermissao(),
                usuario.getCriadoEm(),
                usuario.isAtivo(),
                usuario.getDesativadoEm(),
                VeiculoMapper.toResponseList(usuario.getVeiculos())
            ), usuario.getPersonalDataKeyVersion());    
    }
}
