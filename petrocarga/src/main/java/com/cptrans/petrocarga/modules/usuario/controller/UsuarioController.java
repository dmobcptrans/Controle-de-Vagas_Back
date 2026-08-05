package com.cptrans.petrocarga.modules.usuario.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PatchResponses;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioUtils usuarioUtils;
    private final UsuarioMapper usuarioMapper;

    // GET /usuarios
    @Operation(
            summary = "Listar usuários",
            description = "Retorna uma lista de usuários com base no filtro informado"
        )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping
    public ResponseEntity<PageResponseDTO> getAllUsuarios(
        @Parameter(description = "Nome do usuário")
        @RequestParam(required = false) String nome,

        @Parameter(description = "Email do usuário")
        @RequestParam(required = false) String email,

        @Parameter(description = "Telefone do usuário")
        @RequestParam(required = false) String telefone,

        @Parameter(description = "Permissão do usuário")
        @RequestParam(required = false) List<PermissaoEnum> listaPermissoes,

        @Parameter(description = "Status do usuário (ativo/inativo)")
        @RequestParam(required = false) Boolean ativo,

        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da listagem", example = "ASC")
        @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
        UsuarioFiltrosRequestDTO filtros = new UsuarioFiltrosRequestDTO(nome, email, telefone, listaPermissoes, ativo);

        Page<UsuarioResponseDTO> usuarios = usuarioService.findAll(filtros, pagina, tamanhoPagina, ordem)
                .map((u) -> {
                    String cpfOrCnpj = usuarioUtils.getCpfOrCnpjByPermissaoAndId(u.getPermissao(), u.getId());
                    return usuarioMapper.toResponse(u, cpfOrCnpj);
                });
               
        return ResponseEntity.ok(new PageResponseDTO(usuarios));
    }

    // GET /usuarios/{id}
    @Operation(
        summary = "Visualizar usuário",
        description = "Retorna um usuário com base no id informado"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize(" #id == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(
        @Parameter(description = "ID do usuário")
        @PathVariable UUID id
    ) {
        Usuario usuario = usuarioService.findByIdAndAtivoTrue(id);
        String cpfOrCnpj = usuarioUtils.getCpfOrCnpjByPermissaoAndId(usuario.getPermissao(), usuario.getId());
        return ResponseEntity.ok(usuarioMapper.toResponse(usuario, cpfOrCnpj));
    }

    // PATCH /usuarios/reativar/{id}
    @Operation(
        summary = "Reativar Gestor ou Agente deletado",
        description = "Reativa um usuário com base no id informado"
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/reativar/{id}")
    ResponseEntity<SystemResponse> reativarAgenteOuGestorDeletado(
        @Parameter(description = "ID do usuário")    
        @PathVariable("id") UUID id
    ){
        usuarioService.reativarAgenteOuGestorDeletado(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SystemResponse("Usuário reativado com sucesso!", 201));
    }

}