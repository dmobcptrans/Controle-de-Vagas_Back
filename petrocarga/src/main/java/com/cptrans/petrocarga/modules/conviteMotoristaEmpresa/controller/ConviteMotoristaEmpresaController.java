package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.controller;

import java.util.List;
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
import com.cptrans.petrocarga.enums.StatusConviteMotoristaEmpresaEnum;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.ConviteMotoristaEmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.ConviteMotoristaEmpresaRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.RespostaConviteMotoristaExistenteRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.RespostaConviteNovoMotoristaRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.response.ConviteMotoristaEmpresaResponseDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.service.ConviteMotoristaEmpresaService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/convite-motorista-empresa")
@Tag(name = "Convite Motorista Empresa", description = "Endpoints para gerenciamento de convites de vínculo entre motoristas e empresas")
@RequiredArgsConstructor
public class ConviteMotoristaEmpresaController {
    
    private final ConviteMotoristaEmpresaService service;

    //GET /convite-motorista-empresa/byToken/{conviteToken}
    @Operation(
        summary = "Visualizar convite de vínculo entre motorista e empresa",
        description = "Retorna um convite de vínculo com base no token enviado."
    )
    @GetResponses
    @DefaultResponses
    @GetMapping("/byToken/{conviteToken}")
    public ResponseEntity<ConviteMotoristaEmpresaResponseDTO> getConviteByToken(
        @Parameter(description = "Token do convite")
        @PathVariable String conviteToken
    ) {
        return ResponseEntity.ok(service.getByToken(conviteToken));
    }

    //POST /convite-motorista-empresa/{empresaId}
    @Operation(
        summary = "Gerar convite de vínculo entre motorista e empresa",
        description = "Gera um convite para o motorista aceitar ou recusar o novo vínculo com a empresa."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id")
    @PostMapping("/{empresaId}")
    public ResponseEntity<SystemResponse> gerarConviteMotoristaEmpresa(
        @Parameter(description = "ID da empresa")
        @PathVariable UUID empresaId,

        @Parameter(description = "Dados do convite")
        @Valid @RequestBody ConviteMotoristaEmpresaRequestDTO request
    ) {
        service.convidarMotorista(empresaId, request);
        return ResponseEntity.ok(new SystemResponse("Convite gerado com sucesso, o motorista terá 7 dias para aceitar o pedido.", 201));
    }

    //PATCH /convite-motorista-empresa/responder
    @Operation(
        summary = "Responder convite de vínculo entre motorista e empresa",
        description = "Responde ao convite de vínculo e cria um novo motorista associado à empresa, com base no status e dados enviados."
    )
    @PatchMapping("/responder")
    public ResponseEntity<SystemResponse> responderConviteNovoMotorista(
        @Parameter(description = "Dados da resposta ao convite")
        @Valid @RequestBody RespostaConviteNovoMotoristaRequestDTO request
    ) {
        service.responderConviteNovoMotorista(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SystemResponse("Convite respondido com sucesso!", 201));           
    }

    //PATCH /convite-motorista-empresa/responder/{motoristaId}
    @Operation(
        summary = "Responder convite de vínculo entre motorista e empresa",
        description = "Responde ao convite de vínculo de um motorista existente, com base no status e dados enviados."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("#motoristaId == authentication.principal.id")
    @PatchMapping("/responder/{motoristaId}")
    public ResponseEntity<SystemResponse> responderConvite(
        @Parameter(description = "ID do motorista")
        @PathVariable UUID motoristaId,

        @Parameter(description = "Dados da resposta ao convite")
        @Valid @RequestBody RespostaConviteMotoristaExistenteRequestDTO request
    ) {
        service.responderConviteMotoristaExistente(motoristaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SystemResponse("Convite respondido com sucesso!", 201));           
    }

    //GET /convite-motorista-empresa/byEmpresa/{empresaId}
    @Operation(
        summary = "Visualizar convites de vínculo enviados pela empresa",
        description = "Retorna uma lista paginada de convites de vínculo com base no ID da empresa e filtros opcionais."
    )
    @GetMapping("/byEmpresa/{empresaId}")
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<PageResponseDTO> getConvitesByEmpresa(
        @Parameter(description = "ID da empresa")
        @PathVariable UUID empresaId,

        @Parameter(description = "ID do convite")
        @RequestParam(required = false) UUID conviteId,

        @Parameter(description = "Status do convite")
        @RequestParam(required = false) List<StatusConviteMotoristaEmpresaEnum> listaStatus,

        @Parameter(description = "Nome do motorista")
        @RequestParam(required = false) String nomeMotorista,

        @Parameter(description = "Email do motorista")
        @RequestParam(required = false) String emailMotorista,

        @Parameter(description = "Número da página")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da listagem")
        @RequestParam(defaultValue = "DESC") OrdemEnum ordem
    ){
        ConviteMotoristaEmpresaFiltrosRequestDTO filtros = new ConviteMotoristaEmpresaFiltrosRequestDTO(
            conviteId,
            empresaId, 
            null,
            null,
            listaStatus, 
            null,
            nomeMotorista, 
            emailMotorista
        );

        return ResponseEntity.ok(service.getConvitesByEmpresa(filtros, pagina, tamanhoPagina, ordem));
    }

    //GET /convite-motorista-empresa/byMotorista/{motoristaId}
    @Operation(
        summary = "Visualizar convites de vínculo recebidos pelo motorista",
        description = "Retorna uma lista paginada de convites de vínculo com base no ID do motorista e filtros opcionais."
    )
    @GetMapping("/byMotorista/{motoristaId}")
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#motoristaId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<PageResponseDTO> getConvitesByMotorista(
        @Parameter(description = "ID do motorista")
        @PathVariable UUID motoristaId,

        @Parameter(description = "ID do convite")
        @RequestParam(required = false) UUID conviteId,
        
        @Parameter(description = "Razão social da empresa")
        @RequestParam(required = false) String razaoSocial,

        @Parameter(description = "CNPJ do motorista")
        @RequestParam(required = false) String cnpj,

        @Parameter(description = "Status do convite")
        @RequestParam(required = false) List<StatusConviteMotoristaEmpresaEnum> listaStatus,

        @Parameter(description = "Número da página")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da listagem")
        @RequestParam(defaultValue = "DESC") OrdemEnum ordem
    ){
        ConviteMotoristaEmpresaFiltrosRequestDTO filtros = new ConviteMotoristaEmpresaFiltrosRequestDTO(
            conviteId,
            null, 
            razaoSocial,
            cnpj,
            listaStatus, 
            motoristaId,
            null, 
            null
        );

        return ResponseEntity.ok(service.getConvitesByMotorista(filtros, pagina, tamanhoPagina, ordem));
    }

    @Operation(
        summary = "Cancelar convite de vínculo",
        description = "Cancela um convite de vínculo enviado pela empresa com base no ID do convite."
    )
    @DeleteMapping("/cancelar/{empresaId}")
    @DeleteResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelarConvite(
        @Parameter(description = "ID da empresa")
        @PathVariable UUID empresaId,

        @Parameter(description = "ID do convite")
        @RequestParam UUID conviteId
    ) {
        service.cancelarConvite(empresaId, conviteId);
        return ResponseEntity.noContent().build();
    }
}