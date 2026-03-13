package com.cptrans.petrocarga.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.dto.UsuarioResponseDTO;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.services.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

/**
 * Retorna todos os usuários cadastrados no sistema.
 * 
 * Só permite que os usuários sejam acessados por um usuário com permissão de ADMIN ou GESTOR.
 * @GetMapping
 * @return Lista de usuários encontrados com status ok
 * 
 */
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.findAll().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Retorna um usuário com base no seu id de usuário.
     * Só permite que o usuário seja acessado pelo seu próprio dono ou por um usuário com permissão de ADMIN ou GESTOR.
     * @param id o id do usuário
     * @return o usuário com base no seu id de usuário
     */
    @PreAuthorize(" #id == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable UUID id) {
        Usuario usuario = usuarioService.findById(id);
        return ResponseEntity.ok(usuario.toResponseDTO());
    }

    /**
     * Retorna o CPF de um usuário com base no seu id de usuário.
     * Só permite que o CPF seja visualizado por um usuário com permissão de ADMIN ou GESTOR.
     * @param usuarioId o id do usuário para buscar o CPF
     * @return o CPF do usuário encontrado com status ok
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/cpf/{usuarioId}")
    public ResponseEntity<String> getFullCpfByUsuarioId(@PathVariable UUID usuarioId) {
        String cpf = usuarioService.visualizarCpf(usuarioId);
        return ResponseEntity.ok(cpf);
    }

    /**
     * Retorna o telefone de um usuário com base no seu id de usuário.
     * Só permite que o telefone seja visualizado por um usuário com permissão de ADMIN ou GESTOR.
     * @param usuarioId o id do usuário para buscar o telefone
     * @return o telefone do usuário encontrado com status ok
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/telefone/{usuarioId}")
    public ResponseEntity<String> getFullTelefoneByUsuarioId(@PathVariable UUID usuarioId) {
        String telefone = usuarioService.visualizarTelefone(usuarioId);
        return ResponseEntity.ok(telefone);
    }
}
