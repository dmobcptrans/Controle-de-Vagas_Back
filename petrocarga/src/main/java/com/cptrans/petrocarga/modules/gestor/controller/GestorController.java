package com.cptrans.petrocarga.modules.gestor.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.DeleteResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PatchResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.modules.gestor.dto.mapper.GestorMapper;
import com.cptrans.petrocarga.modules.gestor.dto.request.GestorFiltrosDTO;
import com.cptrans.petrocarga.modules.gestor.dto.request.GestorRequestDTO;
import com.cptrans.petrocarga.modules.gestor.dto.response.GestorResponseDTO;
import com.cptrans.petrocarga.modules.gestor.entity.Gestor;
import com.cptrans.petrocarga.modules.gestor.service.GestorService;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Gestores", description = "Endpoints para gerenciamento de gestores")
@RequestMapping("/gestores")
@RequiredArgsConstructor
public class GestorController {
    private final GestorService gestorService;
    private final GestorMapper gestorMapper;

    // GET /gestores
    @Operation(
        summary = "Listar gestores",
        description = "Retorna uma lista paginada de gestores, permitindo filtros opcionais."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<PageResponseDTO> getAllGestores(
        @Parameter(description = "ID do gestor")
        @RequestParam(required = false) UUID id,

        @Parameter(description = "Nome do gestor")
        @RequestParam(required = false) String nome,

        @Parameter(description = "Telefone do gestor")
        @RequestParam(required = false) String telefone,

        @Parameter(description = "Email do gestor")
        @RequestParam(required = false) String email,

        @Parameter(description = "CPF do gestor")
        @RequestParam(required = false) String cpf,

        @Parameter(description = "Status do gestor (ativo/inativo)")
        @RequestParam(required = false) Boolean ativo,

        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da página", example = "ASC")
        @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
        GestorFiltrosDTO filtros = new GestorFiltrosDTO(id, nome, telefone, email, cpf, ativo);
        return ResponseEntity.ok(gestorService.findAllWithFiltros(filtros, pagina, tamanhoPagina, ordem));
                   
    }

    // GET /gestores/{id}
    @Operation(
        summary = "Visualizar gestor",
        description = "Retorna um gestor com base no seu id."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<GestorResponseDTO> getGestorById(
        @Parameter(description = "ID do gestor")
        @PathVariable UUID id
    ) {
        Gestor gestor = gestorService.findById(id);
        return ResponseEntity.ok(gestorMapper.toResponse(gestor));
    }

    // POST /gestores
    @Operation(
        summary = "Cadastrar gestor",
        description = "Cadastra um novo gestor."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<GestorResponseDTO> createGestor(
        @Parameter(description = "Dados do gestor")
        @Valid @RequestBody GestorRequestDTO request
    ) {
        Gestor gestor = gestorService.createGestor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorMapper.toResponse(gestor));
    }

    // PATCH /gestores/{id}
    @Operation(
        summary = "Atualizar gestor",
        description = "Atualiza as informações de um gestor existente e ativo."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<GestorResponseDTO> updateGestor(
        @Parameter(description = "ID do gestor")
        @PathVariable UUID id, 

        @Parameter(description = "Dados do gestor")
        @Valid @RequestBody UsuarioPATCHRequestDTO gestorRequestDTO
    ) {
        Gestor gestor = gestorService.updateGestor(id, gestorRequestDTO);
        return ResponseEntity.ok(gestorMapper.toResponse(gestor));
    }

    // DELETE /gestores/{id}
    @Operation(
        summary = "Desativar gestor",
        description = "Desativa um gestor existente e ativo."
    )
    @DeleteResponses
    @DefaultResponses
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarbyId(
        @Parameter(description = "ID do gestor")
        @PathVariable UUID id
    ) {
        gestorService.desativarById(id);
        return ResponseEntity.noContent().build();
    }
}