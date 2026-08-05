package com.cptrans.petrocarga.modules.enderecoVaga.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;
import com.cptrans.petrocarga.modules.enderecoVaga.service.EnderecoVagaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Endereco Vaga", description = "Endpoints para gerenciamento de endereços das vagas")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
@RequestMapping("/endereco-vaga")
@RequiredArgsConstructor
public class EnderecoVagaController {
    
    private final EnderecoVagaService enderecoVagaService;
    
    @Operation(
        summary = "Listar os endereços das vagas",
        description = "Retorna todos os endereços cadastrados no sistema."
    )
    @GetResponses
    @DefaultResponses
    @GetMapping
    public ResponseEntity<List<EnderecoVagaResponseDTO>> getEnderecosVaga() {
        return ResponseEntity.ok(enderecoVagaService.getEnderecosVaga());
    }

    @Operation(
        summary = "Listar os códigos PMP",
        description = "Retorna todos os códigos PMP cadastrados no sistema."
    )
    @GetResponses
    @DefaultResponses
    @GetMapping("/codigosPmp")
    public ResponseEntity<Set<String>> getCodigosPmp() {
        return ResponseEntity.ok(enderecoVagaService.getCodigosPmp());
    }

    @Operation(
        summary = "Visualizar o endereço de uma vaga pelo código PMP",
        description = "Retorna o endereço da vaga com base no código PMP informado."
    )
    @GetResponses
    @DefaultResponses
    @GetMapping("/codigosPmp/{codigoPmp}")
    public ResponseEntity<EnderecoVagaResponseDTO> getEnderecoVagaByCodigoPmp(
        @Parameter(description = "Código PMP")
        @PathVariable String codigoPmp
    ) {
        return ResponseEntity.ok(enderecoVagaService.getEnderecoVagaByCodigoPmp(codigoPmp));
    }

}