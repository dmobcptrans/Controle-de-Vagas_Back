package com.cptrans.petrocarga.modules.usuario.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioUtils usuarioUtils;
    private final UsuarioMapper usuarioMapper;

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
    public ResponseEntity<PageResponseDTO> getAllUsuarios(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanhoPagina,
            @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
       Page<UsuarioResponseDTO> usuarios = usuarioService.findAll(pagina, tamanhoPagina, ordem)
                    .map((u) -> {
                        String cpfOrCnpj = usuarioUtils.getCpfOrCnpjByPermissao(u.getPermissao(), u.getId());
                        return usuarioMapper.toResponse(u, cpfOrCnpj);
                    });
               
        return ResponseEntity.ok(new PageResponseDTO(usuarios));
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
        Usuario usuario = usuarioService.findByIdAndAtivoTrue(id);
        String cpfOrCnpj = usuarioUtils.getCpfOrCnpjByPermissao(usuario.getPermissao(), usuario.getId());
        return ResponseEntity.ok(usuarioMapper.toResponse(usuario, cpfOrCnpj));
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
    ResponseEntity<SystemResponse> reativarUsuario(@PathVariable("id") UUID id){
        usuarioService.reativar(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SystemResponse("Usuário reativado com sucesso!", 201));
    }

}