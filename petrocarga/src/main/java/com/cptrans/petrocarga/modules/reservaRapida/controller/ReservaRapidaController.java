package com.cptrans.petrocarga.modules.reservaRapida.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.reservaRapida.dto.mapper.ReservaRapidaMapper;
import com.cptrans.petrocarga.modules.reservaRapida.dto.request.ReservaRapidaRequestDTO;
import com.cptrans.petrocarga.modules.reservaRapida.dto.response.ReservaRapidaResponseDTO;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.reservaRapida.service.ReservaRapidaService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;


@RestController
@Tag(name = "Reservas Rápidas", description = "Endpoints para gerenciamento de reservas rápidas")
@RequestMapping("/reserva-rapida")
@RequiredArgsConstructor
public class ReservaRapidaController {
    private final ReservaRapidaService reservaRapidaService;
    private final ReservaRapidaMapper reservaRapidaMapper;

    @Operation(
        summary = "Criar reserva rápida",
        description = "Endpoint para criar uma reserva rápida"
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @PostMapping()
    public ResponseEntity<ReservaRapidaResponseDTO> createReservaRapida(
        @Parameter(description = "Dados da reserva rápida")
        @RequestBody ReservaRapidaRequestDTO request
    ) {
        ReservaRapida novaReservaRapida = reservaRapidaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaRapidaMapper.toResponse(novaReservaRapida));
    }
    
    @Operation(
        summary = "Listar reservas rápidas feitas por um agente",
        description = "Lista as reservas rápidas feitas por um agente com base no seu ID e filtros opcionais"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#agenteId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{agenteId}")
    public ResponseEntity<PageResponseDTO> getReservasRapidasByagenteId(
        @Parameter(description = "ID do agente")
        @PathVariable UUID agenteId, 
        
        @Parameter(description = "ID da vaga")
        @RequestParam(required = false) UUID vagaId, 
        
        @Parameter(description = "Placa do veículo")
        @RequestParam(required = false) String placaVeiculo, 
        
        @Parameter(description = "Data da reserva")
        @RequestParam(required = false) LocalDate data, 
        
        @Parameter(description = "Status da reserva")
        @RequestParam(required = false) List<StatusReservaEnum> listaStatus, 
        
        @Parameter(description = "Mês da reserva")
        @RequestParam(required = false) Integer mes, 
        
        @Parameter(description = "Ano da reserva")
        @RequestParam(required = false) Integer ano, 
        
        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") Integer numeroPagina, 
        
        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") Integer tamanhoPagina,

        @Parameter(description = "Ordem da listagem", example = "DESC")
        @RequestParam(defaultValue = "DESC") OrdemEnum ordem
    ) {
        DateUtils.validarFiltrosData(data,mes, ano);

        placaVeiculo = placaVeiculo != null ? placaVeiculo.trim().toUpperCase() : null;
        PageResponseDTO reservasRapidas = reservaRapidaService.findByAgenteIdWithFilters(agenteId, vagaId, placaVeiculo, data, listaStatus, mes, ano, numeroPagina, tamanhoPagina, ordem);
        return ResponseEntity.ok().body(reservasRapidas);
    }
    
}