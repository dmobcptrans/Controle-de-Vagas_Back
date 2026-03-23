package com.cptrans.petrocarga.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.GestorFiltrosDTO;
import com.cptrans.petrocarga.application.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;

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

    public Usuario createGestor(Usuario novoGestor) {
        return usuarioService.createUsuario(novoGestor, PermissaoEnum.GESTOR, novoGestor.getCpfHash());
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
