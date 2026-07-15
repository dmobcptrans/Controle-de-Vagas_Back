package com.cptrans.petrocarga.modules.usuario.dto.mapper;



import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class UsuarioMapper {
    private final CriptoUtils criptoUtils;
    private final VeiculoMapper veiculoMapper;

    public UsuarioResponseDTO toResponse(Usuario usuario, String cpfOrCnpj) {
        if (usuario == null) return null;
        cpfOrCnpj = cpfOrCnpj != null && cpfOrCnpj.length() > 14 ? criptoUtils.decrypt(cpfOrCnpj, usuario.getPersonalDataKeyVersion()) : cpfOrCnpj;
        String cpf = cpfOrCnpj != null && cpfOrCnpj.length() == 11 ? cpfOrCnpj : null;
        String cnpj = cpfOrCnpj != null && cpfOrCnpj.length() == 14 ? cpfOrCnpj : null;
        return criptoUtils.decrypt(
            new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTelefoneCripto(),
                usuario.getEmailCripto(),
                cpf,
                cnpj,
                usuario.getPermissao(),
                usuario.getCriadoEm(),
                usuario.getAtivo(),
                usuario.getDesativadoEm(),
                veiculoMapper.toResponseList(usuario.getVeiculosAtivos())
            ), usuario.getPersonalDataKeyVersion());    
    }
}