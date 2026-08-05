package com.cptrans.petrocarga.modules.operacaoVaga.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.response.OperacaoVagaResponseDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.service.OperacaoVagaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;


@RestController
@Tag(name = "Operação Vaga", description = "Endpoints para gerenciamento de operações da vaga (horário de funcionamento)")
@RequestMapping("/operacao-vaga")
@RequiredArgsConstructor
public class OperacaoVagaController {
    private final OperacaoVagaService operacaoVagaService;
    
    @Operation(
        summary = "Listar as operações de uma vaga",
        description = "Retorna as operações da vaga com base no id da vaga informado."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/{vagaId}")
    public ResponseEntity<Set<OperacaoVagaResponseDTO>> getOperacoesVagaByVagaId(
        @Parameter(description = "ID da vaga")
        @PathVariable UUID vagaId
    ) {
        return ResponseEntity.ok(operacaoVagaService.findByVagaId(vagaId));    
    }
}