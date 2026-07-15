package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.request.VeiculoEmpresaMotoristaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response.VeiculoEmpresaMotoristaResponseDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.service.VeiculoEmpresaMotoristaService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "VeiculoEmpresaMotorista", description = "Endpoints para gerenciamento do vínculo entre veículos, empresas e motoristas.")
@RequestMapping("/veiculoEmpresaMotorista")
@RequiredArgsConstructor
public class VeiculoEmpresaMotoristaController {
    private final VeiculoEmpresaMotoristaService service;

    //GET /veiculoEmpresaMotorista/motoristas/{empresaId}/{veiculoId}
    @Operation(
        summary = "Listar motoristas vinculados ao veiculo",
        description = "Listar motoristas vinculados ao veiculo da empresa."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/motoristas/{empresaId}/{veiculoId}")
    public ResponseEntity<PageResponseDTO> listarMotoristasVinculadosAoVeiculo(

        @Parameter(description = "ID da empresa")
        @PathVariable UUID empresaId,
        
        @Parameter(description = "ID do veiculo")
        @PathVariable UUID veiculoId,

        @Parameter(description = "ID do motorista")
        @RequestParam(required = false) UUID motoristaId,

        @Parameter(description = "Nome do motorista")
        @RequestParam(required = false) String motoristaNome,

        @Parameter(description = "CPF do motorista")
        @RequestParam(required = false) String motoristaCpf,

        @Parameter(description = "Telefone do motorista")
        @RequestParam(required = false) String motoristaTelefone,

        @Parameter(description = "Email do motorista")
        @RequestParam(required = false) String motoristaEmail,

        @Parameter(description = "Status do motorista (ativo/inativo)")
        @RequestParam(required = false) Boolean motoristaAtivo,

        @Parameter(description = "Pagina", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por pagina", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem", example = "ASC")
        @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
        VeiculoEmpresaMotoristaFiltrosRequestDTO filtros = new VeiculoEmpresaMotoristaFiltrosRequestDTO(
            veiculoId, 
            null, 
            null, 
            null, 
            null, 
            null, 
            empresaId,
            null,
            null,
            motoristaId, 
            motoristaNome,
            motoristaCpf, 
            motoristaTelefone, 
            motoristaEmail,
            motoristaAtivo
        );
        return ResponseEntity.ok(service.findMotoristasByEmpresaIdAndVeiculoId(filtros, pagina, tamanhoPagina, ordem));
    }

    //GET /veiculoEmpresaMotorista/veiculos/{motoristaId}
    @Operation(
        summary = "Listar veiculos associados ao motorista",
        description = "Listar veiculos associados ao motorista da empresa."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or #motoristaId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/veiculos/{empresaId}/{motoristaId}")
    public ResponseEntity<PageResponseDTO> listarVeiculosAssociadosAoMotorista(

        @Parameter(description = "ID da empresa")
        @PathVariable UUID empresaId,

        @Parameter(description = "ID do motorista")
        @PathVariable UUID motoristaId,
    
        @Parameter(description = "ID do veiculo")
        @RequestParam(required = false) UUID veiculoId,

        @Parameter(description = "Placa do veiculo")
        @RequestParam(required = false) String placa,

        @Parameter(description = "Marca do veiculo")
        @RequestParam(required = false) String marca,

        @Parameter(description = "Modelo do veiculo")
        @RequestParam(required = false) String modelo,

        @Parameter(description = "Tipo do veiculo")
        @RequestParam(required = false) TipoVeiculoEnum tipoVeiculo,

        @Parameter(description = "Status do veiculo (ativo/inativo)")
        @RequestParam(required = false) Boolean ativo,

        @Parameter(description = "Página", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem", example = "ASC")
        @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
        VeiculoEmpresaMotoristaFiltrosRequestDTO filtros = new VeiculoEmpresaMotoristaFiltrosRequestDTO(
            veiculoId,
            placa,
            marca,
            modelo,
            tipoVeiculo,
            ativo,
            empresaId,
            null,
            null,
            motoristaId,
            null,
            null,
            null,
            null,
            null
        );
        return ResponseEntity.ok(service.findVeiculosByMotoristaId(filtros, pagina, tamanhoPagina, ordem));
    }

    //POST /veiculoEmpresaMotorista
    @Operation(
        summary = "Vincular motorista ao veiculo",
        description = "Vincular um motorista da empresa à um veiculo da mesma empresa."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/vincular/{empresaId}")
    public ResponseEntity<VeiculoEmpresaMotoristaResponseDTO> vincularMotoristaAoVeiculo(@PathVariable UUID empresaId, @RequestParam UUID veiculoId, @RequestParam UUID motoristaId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.vincularMotoristaAoVeiculo(empresaId, veiculoId, motoristaId));
    }

    //GET /veiculoEmpresaMotorista/desvincular
    @Operation(
        summary = "Desvincular motorista do veiculo",
        description = "Desvincular o motorista de um veiculo da empresa."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("#empresaId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/desvincular/{empresaId}")
    public ResponseEntity<Void> desvincularMotoristaDoVeiculo(@PathVariable UUID empresaId, @RequestParam UUID veiculoId, @RequestParam UUID motoristaId) {
        service.desvincularMotoristaDoVeiculo(empresaId, veiculoId, motoristaId);
        return ResponseEntity.noContent().build();
    }
    
}