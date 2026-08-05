package com.cptrans.petrocarga.modules.empresa.controller;


import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PatchResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.modules.empresa.dto.mapper.EmpresaMapper;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.empresa.service.EmpresaService;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@Tag(name = "Empresas", description = "Endpoints para gerenciamento de empresas")
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EmpresaMapper empresaMapper;

    // GET /empresas
    @Operation(
        summary = "Listar empresas",
        description = "Retorna uma lista paginada de empresas, permitindo filtros opcionais."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping
    public ResponseEntity<PageResponseDTO> getAllEmpresas(
        @Parameter(description = "ID da empresa")
        @RequestParam(required = false) UUID empresaId,

        @Parameter(description = "CNPJ da empresa")
        @RequestParam(required = false) String cnpj,

        @Parameter(description = "Nome da empresa")
        @RequestParam(required = false) String nome,

        @Parameter(description = "Telefone da empresa")
        @RequestParam(required = false) String telefone,

        @Parameter(description = "Status da empresa (ativo/inativo)")
        @RequestParam(required = false) Boolean ativo,

        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da listagem", example = "ASC")
        @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
        EmpresaFiltrosRequestDTO filtros = new EmpresaFiltrosRequestDTO(empresaId, cnpj, nome, telefone, ativo);
        return ResponseEntity.ok(empresaService.listarEmpresas(filtros, pagina, tamanhoPagina, ordem));
    }

    // GET /empresas/{id}
    @Operation(
        summary = "Visualizar empresa",
        description = "Retorna uma empresa com base no seu id."
    )
    @PreAuthorize("#id == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> getEmpresaById(
        @Parameter(description = "ID da empresa")
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(empresaMapper.toResponse(empresaService.findByIdAndAtivoTrue(id)));
    }

    // POST /empresas/cadastro
    @Operation(
        summary = "Cadastrar empresa",
        description = "Cadastrar uma nova empresa."
    )
    @PostResponses
    @DefaultResponses
    @PostMapping("/cadastro")
    public ResponseEntity<EmpresaResponseDTO> createEmpresa(
        @Parameter(description = "Dados da empresa")
        @RequestBody @Valid EmpresaRequestDTO empresaRequestDTO
    ) {
        EmpresaResponseDTO response = empresaService.create(empresaRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PATCH /empresas/{id}
    @Operation(
        summary = "Atualizar empresa",
        description = "Atualiza as informações de uma empresa existente e ativa."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("#id == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> updateEmpresa(
        @Parameter(description = "ID da empresa")
        @PathVariable UUID id, 
        
        @Parameter(description = "Dados da empresa")
        @Valid @RequestBody UsuarioPATCHRequestDTO request
    ) {
        EmpresaResponseDTO response = empresaService.update(id, request);
        return ResponseEntity.ok(response);
    }
}