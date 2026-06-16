package com.cptrans.petrocarga.modules.gestor.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.gestor.dto.request.GestorFiltrosDTO;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;

@Service
public class GestorService {
    @Autowired
    private UsuarioService usuarioService;

    public List<Usuario> findAll() {
        return usuarioService.findByPermissao(PermissaoEnum.GESTOR);
    }

    public List<Usuario> findAllByAtivo(Boolean ativo) {
        return usuarioService.findByPermissaoAndAtivo(PermissaoEnum.GESTOR, ativo);
    }

    public Usuario findByUsuarioId(UUID usuarioId) {
        return usuarioService.findByIdAndAtivo(usuarioId, true);
    }

    public Usuario createGestor(UsuarioRequestDTO request) {
        return usuarioService.createUsuario(request, PermissaoEnum.GESTOR);
    }

    public Usuario updateGestor(UUID id, UsuarioPATCHRequestDTO novoGestor) {
        return usuarioService.patchUpdate(id, PermissaoEnum.GESTOR, novoGestor);
    }

    public void deleteByUsuarioId(UUID usuarioId) {
        usuarioService.deleteById(usuarioId);
    }

    public List<Usuario> findAllWithFiltros(GestorFiltrosDTO filtros) {
        return usuarioService.findAllGestoresWithFiltros(filtros);
    }
}
