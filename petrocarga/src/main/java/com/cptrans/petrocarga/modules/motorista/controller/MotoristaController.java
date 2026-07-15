package com.cptrans.petrocarga.modules.motorista.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.cptrans.petrocarga.modules.motorista.dto.mapper.MotoristaMapper;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaEmpresaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaFiltrosDTO;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResponseDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.service.MotoristaService;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/motoristas")
@RequiredArgsConstructor
public class MotoristaController {
    private final MotoristaService motoristaService;
    private final MotoristaMapper motoristaMapper;
    
    //GET /motoristas
    @Operation(
        summary = "Listar motoristas",
        description = "Retorna uma lista de motoristas com paginação, ordenação e filtros opcionais."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping
    public ResponseEntity<PageResponseDTO> getAllMotoristas(

        @Parameter(description = "ID do motorista")
        @RequestParam(required = false) UUID id,

        @Parameter(description = "Nome do motorista")
        @RequestParam(required = false) String nome,

        @Parameter(description = "Telefone do motorista")
        @RequestParam(required = false) String telefone,

        @Parameter(description = "Email do motorista")
        @RequestParam(required = false) String email,

        @Parameter(description = "CPF do motorista")
        @RequestParam(required = false) String cpf,

        @Parameter(description = "CNH do motorista")
        @RequestParam(required = false) String cnh,

        @Parameter(description = "Id da empresa associada ao motorista")
        @RequestParam(required = false) UUID empresaId,

        @Parameter(description = "CNPJ da Empresa associada ao motorista")
        @RequestParam(required = false) String empresaCnpj,

        @Parameter(description = "Razão social da Empresa associada ao motorista")
        @RequestParam(required = false) String empresaRazaoSocial,

        @Parameter(description = "Status do motorista (ativo/inativo)")
        @RequestParam(required = false) Boolean ativo,

        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da listagem", example = "ASC")
        @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
        MotoristaFiltrosDTO filtros = new MotoristaFiltrosDTO(id, nome, telefone, email, cpf, cnh, empresaId, empresaCnpj, empresaRazaoSocial, ativo);
        PageResponseDTO motoristasFiltrados = motoristaService.findAllWithFiltros(filtros, pagina, tamanhoPagina, ordem);
        return ResponseEntity.ok(motoristasFiltrados);
    }

    //GET /motoristas/{id}
    @Operation(
        summary = "Buscar motorista por ID",
        description = "Retorna os dados de um motorista a partir do ID."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<MotoristaResponseDTO> getMotoristaById(@PathVariable UUID id, @RequestParam(required = false, defaultValue = "true") Boolean ativo) {
        Motorista motorista = motoristaService.findByIdAndAtivo(id, ativo);
        return ResponseEntity.ok(motoristaMapper.toResponse(motorista));

    }

    //GET /motorista/byEmpresa/{empresaId}
    @Operation(
        summary = "Buscar motorista da empresa",
        description = "Retorna os dados de um motorista a partir do ID da empresa."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/byEmpresa/{empresaId}")
    public ResponseEntity<PageResponseDTO> getMotoristaByEmpresaId(
            @PathVariable UUID empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanhoPagina,
            @RequestParam(defaultValue = "ASC") OrdemEnum ordem
        ) {
    
        return ResponseEntity.ok(motoristaService.findByEmpresaId(empresaId, pagina, tamanhoPagina, ordem));
    }

    //POST /motoristas/cadastro
    @Operation(
        summary = "Cadastrar motorista",
        description = "Realiza o cadastro de um novo motorista."
    )
    @PostResponses
    @DefaultResponses
    @PostMapping("/cadastro")
    public ResponseEntity<MotoristaResponseDTO> createMotorista(@RequestBody @Valid MotoristaRequestDTO request) {
        Motorista motorista = motoristaService.createMotorista(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(motoristaMapper.toResponse(motorista));

    }

    //POST /motoristas/cadastroEmpresa/{empresaUsuarioId}
    @Operation(
        summary = "Cadastrar motorista por empresa",
        description = "Realiza o cadastro de um novo motorista associado à uma empresa."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping("/cadastroEmpresa/{empresaId}")
    public ResponseEntity<MotoristaResponseDTO> createMotoristaEmpresa(@PathVariable UUID empresaId,@RequestBody @Valid MotoristaEmpresaRequestDTO request) {
        Motorista motorista = motoristaService.createMotoristaByEmpresa(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(motoristaMapper.toResponse(motorista));

    }

    //PATCH /motoristas/desvincularEmpresa/{motoristaId}
    @Operation(
        summary = "Desvincular motorista da empresa",
        description = "Desvincula um motorista de uma empresa."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/desvincularEmpresa/{empresaId}/{motoristaId}")
    public ResponseEntity<SystemResponse> desvincularMotoristaEmpresa(
        @Parameter(description = "ID da empresa")
        @PathVariable UUID empresaId,

        @Parameter(description = "ID do motorista")
        @PathVariable UUID motoristaId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(motoristaService.desvincularMotoristaEmpresa(empresaId,motoristaId));
    }


    //PATCH /motoristas/{id}
    @Operation(
        summary = "Atualizar motorista",
        description = "Atualiza as informações de um motorista existente e ativo à partir do ID."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<MotoristaResponseDTO> updateMotorista(@AuthenticationPrincipal UserAuthenticated usuarioAutenticado, @PathVariable UUID id,  @RequestBody @Valid UsuarioPATCHRequestDTO motoristaRequestDTO) {
        Motorista motorista = motoristaService.updateMotorista(usuarioAutenticado, id, motoristaRequestDTO);
        return ResponseEntity.ok(motoristaMapper.toResponse(motorista));

    }

    //DELETE /motoristas/{id}
    @Operation(
        summary = "Desativar motorista",
        description = "Desativa um motorista à partir do ID."
    )
    @DeleteResponses
    @DefaultResponses
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarById(@PathVariable UUID id) {
        motoristaService.desativarById(id);
        return ResponseEntity.noContent().build();
    }
}