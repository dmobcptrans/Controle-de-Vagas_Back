package com.cptrans.petrocarga.modules.usuario.utils;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.repository.VeiculoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioUtils {
    public final static String VERSAO_ATUAL_TERMOS = "1.0.0";
    private final VeiculoRepository veiculoRepository;
    public static String gerarLastN(String string, int n) {
        return string.substring(string.length() - n);
    }

    public Boolean possuiVeiculoCadastrado(UUID usuarioId, PermissaoEnum permissao) {
        List<PermissaoEnum> permissoes = List.of(PermissaoEnum.MOTORISTA, PermissaoEnum.EMPRESA);
        if (permissoes.contains(permissao)) return veiculoRepository.existsByUsuarioIdAndAtivo(usuarioId, true);
        return false;
    }

}