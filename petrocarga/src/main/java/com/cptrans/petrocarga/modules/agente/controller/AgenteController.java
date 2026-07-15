package com.cptrans.petrocarga.modules.agente.controller;

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
import com.cptrans.petrocarga.modules.agente.dto.mapper.AgenteMapper;
import com.cptrans.petrocarga.modules.agente.dto.request.AgenteFiltrosDTO;
import com.cptrans.petrocarga.modules.agente.dto.request.AgenteRequestDTO;
import com.cptrans.petrocarga.modules.agente.dto.response.AgenteResponseDTO;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.agente.service.AgenteService;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/agentes")
@Tag(name = "Agentes", description = "Endpoints para gerenciamento de agentes")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
@RequiredArgsConstructor
public class AgenteController {

    private final AgenteService agenteService;
    private final AgenteMapper agenteMapper;

    //GET /agentes
    @Operation(
        summary = "Listar agentes",
        description = "Retorna uma lista paginada de agentes, permitindo filtros por nome, matrícula e status (ativo ou inativo)."
    )
    @GetResponses
    @DefaultResponses
    @GetMapping
    public ResponseEntity<PageResponseDTO> getAllAgentes(
            @Parameter(description = "Nome do agente")
            @RequestParam(required = false) String nome,

            @Parameter(description = "Matrícula do agente")
            @RequestParam(required = false) String matricula,

            @Parameter(description = "Status do agente (ativo/inativo)")
            @RequestParam(required = false) Boolean ativo,

            @Parameter(description = "Número da página", example = "0")
            @RequestParam(defaultValue = "0") int pagina,

            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam(defaultValue = "10") int tamanhoPagina,

            @Parameter(description = "Ordem da listagem", example = "ASC")
            @RequestParam(defaultValue = "ASC") OrdemEnum ordem
        ) {
        AgenteFiltrosDTO filtros = new AgenteFiltrosDTO(nome, matricula, ativo);
        return ResponseEntity.ok(agenteService.findByFiltros(filtros, pagina, tamanhoPagina, ordem));
    }

    //GET /agentes/{id}
    @Operation(
        summary = "Buscar agente por ID",
        description = "Retorna os dados de um agente a partir do ID do usuário."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize(" #id == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<AgenteResponseDTO> getAgenteById(
            @Parameter(description = "ID do agente", required = true)
            @PathVariable UUID id
        ) {
        Agente agente = agenteService.findByIdAndAtivoTrue(id);
        return ResponseEntity.ok(agenteMapper.toResponse(agente));
    }

    //POST /agentes
    @Operation(
        summary = "Cadastrar agente",
        description = "Realiza o cadastro de um novo agente."
    )
    @PostResponses
    @DefaultResponses
    @PostMapping
    public ResponseEntity<AgenteResponseDTO> createAgente(
            @Valid @RequestBody AgenteRequestDTO agenteRequestDTO
        ) {
        Agente savedAgente = agenteService.createAgente(agenteRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(agenteMapper.toResponse(savedAgente));
    }

    //PATCH /agentes/{id}
    @Operation(
        summary = "Atualizar agente",
        description = "Atualiza as informações de um agente existente e ativo."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize(" #id == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<AgenteResponseDTO> updateAgenteById(@PathVariable UUID id, @RequestBody @Valid UsuarioPATCHRequestDTO agenteRequestDTO) {
        Agente updatedAgente = agenteService.updateAgenteById(id, agenteRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(agenteMapper.toResponse(updatedAgente));
    }

    @Operation(
        summary = "Desativar agente",
        description = "Desativa um agente com base no seu id de usuário."
    )
    @DeleteResponses
    @DefaultResponses
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> desativarAgente(@PathVariable UUID usuarioId) {
        agenteService.desativarById(usuarioId);
        return ResponseEntity.noContent().build();
    }
}