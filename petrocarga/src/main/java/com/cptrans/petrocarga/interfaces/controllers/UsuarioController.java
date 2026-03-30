package com.cptrans.petrocarga.interfaces.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.application.dto.ApiResponse;
import com.cptrans.petrocarga.application.dto.UsuarioResponseDTO;
import com.cptrans.petrocarga.application.usecase.UsuarioService;
import com.cptrans.petrocarga.domain.entities.Usuario;

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
     * Reativa um usuário préviamente desativado com base no seu id de usuário.
     * Só permite que o usuário seja acessado por um usuário com permissão de ADMIN ou GESTOR.
     * Só permite reativar usuários com permissão AGENTE ou GESTOR e que tenham sido desativados anteriomente.
     * @param id o id do usuário
     * @return mensagem de sucesso ou usário não encontrado
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/reativar/{id}")
    ResponseEntity<ApiResponse> reativarUsuario(@PathVariable("id") UUID id){
        usuarioService.reativar(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Usuário reativado com sucesso!"));
    }

}
