package com.cptrans.petrocarga.modules.disponibilidadeVaga.controller;


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
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.mapper.DisponibilidadeVagaMapper;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.request.DisponibilidadeVagaRequestDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.request.MultiplasDisponibilidadesVagaRequestDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.response.DisponibilidadeVagaResponseDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.response.DisponibilidadeVagaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.entity.DisponibilidadeVaga;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.service.DisponibilidadeVagaService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Disponibilidade Vaga", description = "Endpoint para gerenciamento de disponibilidade de vagas")
@RequestMapping("/disponibilidade-vagas")
@RequiredArgsConstructor
public class DisponibilidadeVagaController {

    private final DisponibilidadeVagaService disponibilidadeVagaService;
    private final DisponibilidadeVagaMapper disponibilidadeVagaMapper;

    // GET /disponibilidade-vagas
    @Operation(
        summary = "Listar todas as disponibilidades de vagas",
        description = "Retorna todas as disponibilidades de vagas de forma paginada caso não haja filtros de data, se houver, retorna uma lista filtrada."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping
    public ResponseEntity<?> getAllDisponibilidadeVagas(
        @Parameter(description = "ID da vaga")
        @RequestParam(required = false) UUID vagaId,

        @Parameter(description = "Mês")
        @RequestParam(required = false) Integer mes, 
        
        @Parameter(description = "Ano")
        @RequestParam(required = false) Integer ano,

        @Parameter(description = "Página", example = "0")
        @RequestParam(defaultValue = "0") Integer pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") Integer tamanhoPagina,

        @Parameter(description = "Ordem da paginação", example = "DESC")
        @RequestParam(defaultValue = "DESC") OrdemEnum ordem
    
    ) {
        if (mes != null || ano != null){
            List<DisponibilidadeVaga> disponibilidadesVaga = disponibilidadeVagaService.findByOptionalVagaIdAndMesEAno(vagaId, mes, ano);
            return ResponseEntity.ok(disponibilidadeVagaMapper.toResponseList(disponibilidadesVaga));
        }    
        PageResponseDTO response = disponibilidadeVagaService.findAllPaginadoWithOptionalVagaId(vagaId, pagina, tamanhoPagina, ordem);
        return ResponseEntity.ok(response);
    }

    // GET /disponibilidade-vagas/{id}
    @Operation(
        summary = "Visualizar uma disponibilidade de vaga específica",
        description = "Retorna uma disponibilidade de vaga com base no id informado."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/{id}")
    public ResponseEntity<DisponibilidadeVagaResponseDTO> getDisponibilidadeVagaById(
        @Parameter(description = "ID da DisponibilidadeVaga")
        @PathVariable UUID id
    ) {
        DisponibilidadeVaga disponibilidadeVaga = disponibilidadeVagaService.findById(id);
        return ResponseEntity.ok(disponibilidadeVagaMapper.toResponse(disponibilidadeVaga));
    }

    // GET /disponibilidade-vagas/vaga/{vagaId}
    @Operation(
        summary = "Listar as disponibilidades de uma vaga",
        description = "Retorna as disponibilidades de vagas com base no id da vaga informado."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/vaga/{vagaId}")
    public ResponseEntity<List<DisponibilidadeVagaResponseDTO>> getDisponibilidadeVagaByVagaId(
        @Parameter(description = "ID da vaga")    
        @PathVariable UUID vagaId
    ) {
        List<DisponibilidadeVagaResponseDTO> disponibilidadeVaga = disponibilidadeVagaMapper.toResponseList(disponibilidadeVagaService.findByVagaId(vagaId));
        return ResponseEntity.ok(disponibilidadeVaga);
    }

    // GET /disponibilidade-vagas/resumo/{vagaId}
    @Operation(
        summary = "Listar as disponibilidades de uma vaga de forma resumida",
        description = "Retorna as disponibilidades de vagas de forma resumida com base no id da vaga informado e os filtros de mês e ano."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/resumo/{vagaId}")
    public ResponseEntity<List<DisponibilidadeVagaSimplificadoResponseDTO>> getDisponibilidadeVagaSimplificadoByVagaId(
        @Parameter(description = "ID da vaga")    
        @PathVariable UUID vagaId,

        @Parameter(description = "Mês")
        @RequestParam(required = true) Integer mes,

        @Parameter(description = "Ano")
        @RequestParam(required = true) Integer ano
    ){
        DateUtils.validarFiltroDeMesEAno(mes, ano);
        List<DisponibilidadeVagaSimplificadoResponseDTO> disponibilidadesVaga = disponibilidadeVagaService.getDisponibilidadeVagaSimplificadoByVagaIdMesEAno(vagaId, mes, ano);
        return ResponseEntity.ok(disponibilidadesVaga);
    }

    // POST /disponibilidade-vagas
    @Operation(
        summary = "Criar uma nova disponibilidade de vaga",
        description = "Cria uma nova disponibilidade de vaga com base nos parâmetros informados."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<DisponibilidadeVagaResponseDTO> createDisponibilidadeVaga(
        @Parameter(description = "Dados da disponibilidade de vaga")
        @Valid @RequestBody DisponibilidadeVagaRequestDTO disponibilidadeVagaRequestDTO
    ) {
        DisponibilidadeVaga savedDisponibilidadeVaga = disponibilidadeVagaService.createDisponibilidadeVaga(disponibilidadeVagaRequestDTO, disponibilidadeVagaRequestDTO.getVagaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidadeVagaMapper.toResponse(savedDisponibilidadeVaga));
    }

    // POST /disponibilidade-vagas/vagas
    @Operation(
        summary = "Criar múltiplas disponibilidades de vaga",
        description = "Cria múltiplas disponibilidades de vaga com base nos parâmetros informados."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/vagas")
    public ResponseEntity<List<DisponibilidadeVagaResponseDTO>> createMultipleDisponibilidadeVagas(
        @Parameter(description = "Dados das disponibilidades de vaga")
        @Valid @RequestBody MultiplasDisponibilidadesVagaRequestDTO multiplasDisponibilidadesVagaRequestDTO
    ) {
        List<DisponibilidadeVaga> savedDisponibilidadeVagas = disponibilidadeVagaService.createMultipleDisponibilidadeVagas(multiplasDisponibilidadesVagaRequestDTO);
        List<DisponibilidadeVagaResponseDTO> response = disponibilidadeVagaMapper.toResponseList(savedDisponibilidadeVagas);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PATCH /disponibilidade-vagas/{id}
    @Operation(
        summary = "Atualizar uma disponibilidade de vaga",
        description = "Atualiza uma disponibilidade de vaga com base no ID e nos parâmetros informados."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<DisponibilidadeVagaResponseDTO> updateDisponibilidadeVaga(
        @Parameter(description = "ID da DisponibilidadeVaga")
        @PathVariable UUID id, 
        
        @Parameter(description = "Dados da disponibilidade de vaga")
        @RequestBody DisponibilidadeVagaRequestDTO request
    ) {
        DisponibilidadeVaga disponibilidadeVaga = disponibilidadeVagaService.updateDisponibilidadeVaga(id, request.getVagaId(), request.getInicio(), request.getFim());
        return ResponseEntity.ok(disponibilidadeVagaMapper.toResponse(disponibilidadeVaga));
      
    }

    // PATCH /disponibilidade-vagas/byList
    @Operation(
        summary = "Atualizar múltiplas disponibilidades de vaga",
        description = "Atualiza múltiplas disponibilidades de vaga com base na lista de IDs e nos parâmetros informados."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/byList")
    public ResponseEntity<List<DisponibilidadeVagaResponseDTO>> updateDisponibilidadeVagaByList(
        @Parameter(description = "Lista de IDs")
        @RequestParam(required = true) List<UUID> listaIds,

        @Parameter(description = "Dados da disponibilidade de vaga")
        @RequestBody  DisponibilidadeVagaRequestDTO disponibilidadeVagaRequestDTO
    ) {
        List<DisponibilidadeVaga> disponibilidadeVaga = disponibilidadeVagaService.updateDisponibilidadeVagaByIdList(disponibilidadeVagaRequestDTO, listaIds);
        return ResponseEntity.ok(disponibilidadeVagaMapper.toResponseList(disponibilidadeVaga));
      
    }

    // DELETE /disponibilidade-vagas/{id}
    @Operation(
        summary = "Deletar uma disponibilidade de vaga",
        description = "Deleta uma disponibilidade de vaga com base no ID informado."
    )
    @DeleteResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisponibilidadeVaga(
        @Parameter(description = "ID da DisponibilidadeVaga")
        @PathVariable UUID id
    ) {
        disponibilidadeVagaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    // DELETE /disponibilidade-vagas/byList
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/byList")
    public ResponseEntity<Void> deleteMultiplasDisponibilidadeVaga(
        @Parameter(description = "Lista de IDs")
        @RequestParam(required = true) List<UUID> listaIds
    ) {
        disponibilidadeVagaService.deleteByIdList(listaIds);
        return ResponseEntity.noContent().build();
    }
}