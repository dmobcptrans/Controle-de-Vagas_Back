package com.cptrans.petrocarga.modules.vaga.controller;

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
import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.vaga.dto.mapper.VagaMapper;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaPatchDTO;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaCoordenadaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.exceptions.VagaExceptions;
import com.cptrans.petrocarga.modules.vaga.service.VagaService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; 

@RestController
@Tag(name = "Vagas", description = "Endpoints para gerenciamento de vagas")
@RequestMapping("/vagas")
@RequiredArgsConstructor
public class VagaController {
    
    private final VagaService vagaService;
    private final VagaMapper vagaMapper;

    //GET /vagas/all
    @Operation(
        summary = "Listar todas as vagas.",
        description = "Retorna uma lista de todas as vagas registradas."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE','MOTORISTA','EMPRESA')")
    @GetMapping("/all")
    public ResponseEntity<List<VagaResponseDTO>> findAll(
        @Parameter(description = "Status da vaga")
        @RequestParam(required = false) StatusVagaEnum status
    ) { 
        if (status != null) {
            List<VagaResponseDTO> vagas = vagaMapper.toResponseList(vagaService.findAllByStatus(status));
            return ResponseEntity.ok(vagas);
        }
        List<VagaResponseDTO> vagas = vagaMapper.toResponseList(vagaService.findAll());
        return ResponseEntity.ok(vagas);
    }

    // GET /vagas/mapa
    @Operation(
        summary = "Buscar vagas por área do mapa.",
        description = "Retorna vagas dentro da área visível do mapa com base nos limites geográficos (bounding box)."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE','MOTORISTA','EMPRESA')")
    @GetMapping("/mapa")
    public ResponseEntity<List<VagaCoordenadaResponseDTO>> buscarPorMapa(
        @Parameter(description = "Norte")
        @RequestParam Double north,

        @Parameter(description = "Sul")
        @RequestParam Double south,

        @Parameter(description = "Leste")
        @RequestParam Double east,

        @Parameter(description = "Oeste")
        @RequestParam Double west,

        @Parameter(description = "Status da vaga")
        @RequestParam(required = false) StatusVagaEnum status
    ) {
        List<VagaCoordenadaResponseDTO> vagas;

        if (north < south || east < west) throw new VagaExceptions.BoundingBoxInvalidoException();

        StatusVagaEnum statusBusca = status != null ? status : StatusVagaEnum.DISPONIVEL;

        vagas = vagaMapper.toCoordenadaResponseList(vagaService.buscarPorMapa(north, south, east, west, statusBusca));
      
        return ResponseEntity.ok(vagas);
    }

    // GET /vagas
    @GetMapping()
    @Operation(
        summary = "Listar todas as vagas com paginação",
        description = "Retorna uma lista paginada de todas as vagas disponíveis, com filtros opcionais."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE','MOTORISTA','EMPRESA')")
    public ResponseEntity<PageResponseDTO> findAllPaginadas(
        @Parameter(description = "Código PMP")
        @RequestParam(required = false) String codigoPmp,

        @Parameter(description = "Logradouro")
        @RequestParam(required = false) String logradouro,

        @Parameter(description = "Bairro")
        @RequestParam(required = false) String bairro,

        @Parameter(description = "Área")
        @RequestParam(required = false) AreaVagaEnum area,

        @Parameter(description = "Tipo da Vaga")
        @RequestParam(required = false) TipoVagaEnum tipo,

        @Parameter(description = "Status da vaga")
        @RequestParam(required = false) StatusVagaEnum status,
        
        @Parameter(description = "Página", example = "0")
        @RequestParam(defaultValue = "0") Integer numeroPagina, 

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") Integer tamanhoPagina, 

        @Parameter(description = "Ordem", example = "ASC")
        @RequestParam(defaultValue = "ASC") OrdemEnum ordem

    ) {
        VagaFiltrosRequestDTO filtros = new VagaFiltrosRequestDTO(codigoPmp, logradouro, bairro, area, tipo, status);
    	PageResponseDTO vagasPaginadas = vagaService.findAllPaginadas(filtros, numeroPagina, tamanhoPagina, ordem);
        return ResponseEntity.ok(vagasPaginadas);
    }

    // GET /vagas/{id}
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar uma vaga pelo ID",
        description = "Retorna os detalhes de uma vaga específica identificada pelo seu UUID."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE','MOTORISTA','EMPRESA')")
    public ResponseEntity<VagaResponseDTO> findById(
        @Parameter(description = "ID da vaga")
        @Valid @PathVariable UUID id
    ) {
        return ResponseEntity.ok(vagaMapper.toResponse(vagaService.findById(id)));
    }

    // GET /vagas/resumo/{id}
    @GetMapping("/resumo/{id}")
    @Operation(
        summary = "Buscar uma vaga pelo ID de forma resumida",
        description = "Retorna o resumo de uma vaga específica identificada pelo seu UUID."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE','MOTORISTA','EMPRESA')")
    public ResponseEntity<VagaSimplificadoResponseDTO> findSimplificadoById(
        @Parameter(description = "ID da vaga")
        @Valid @PathVariable UUID id
    ) {
        return ResponseEntity.ok(vagaMapper.toResponseSimplificado(vagaService.findById(id)));
    }

    // POST /vagas
    @Operation(
        summary = "Cadastrar uma nova vaga",
        description = "Cria uma nova vaga com base nos dados fornecidos no corpo da requisição.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados necessários para criação de uma vaga",
            required = true,
            content = @Content(schema = @Schema(implementation = VagaRequestDTO.class))
        )
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping()
    public ResponseEntity<VagaResponseDTO> createVaga(
        @Parameter(description = "Dados necessários para criação de uma vaga")
        @Valid @RequestBody VagaRequestDTO vagaRequest
    ) {
        Vaga vaga = vagaService.createVaga(vagaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(vagaMapper.toResponse(vaga));
    }
    
    // DELETE /vagas/{id}
    @Operation(
        summary = "Deletar uma vaga pelo ID",
        description = "Remove uma vaga específica identificada pelo seu UUID."
    )
    @DeleteResponses
    @DefaultResponses
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
        @Parameter(description = "ID da vaga")
        @PathVariable UUID id
    ) {
        vagaService.deleteById(id);
        return ResponseEntity.noContent().build(); 
    }
    
    // PATCH /vagas/{id}
    @Operation(
        summary = "Atualizar parcialmente uma vaga",
        description = "Atualiza apenas os campos enviados no corpo da requisição para a vaga especificada pelo ID.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Campos a serem atualizados na vaga",
            required = true,
            content = @Content(schema = @Schema(implementation = VagaRequestDTO.class))
        )
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<VagaResponseDTO> updateById(
        @Parameter(description = "ID da vaga")
        @PathVariable UUID id, 
        
        @Parameter(description = "Campos a serem atualizados na vaga")
        @RequestBody VagaPatchDTO vagaRequest
    ) {
        Vaga vagaAtualizada = vagaService.updateById(id, vagaRequest);
        return ResponseEntity.ok(vagaMapper.toResponse(vagaAtualizada));
    }

}