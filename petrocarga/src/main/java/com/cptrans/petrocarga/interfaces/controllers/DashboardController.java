package com.cptrans.petrocarga.interfaces.controllers;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.application.dto.dashboard.DashboardKpiDTO;
import com.cptrans.petrocarga.application.dto.dashboard.DashboardSummaryDTO;
import com.cptrans.petrocarga.application.usecase.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
@Tag(name = "Dashboard", description = "Endpoints para visualização de métricas e KPIs do sistema")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Retorna KPIs, tipos de veículos, bairros e cidades de origem.
     * Se os parâmetros de data não forem informados, assume o dia atual (00:00 até 23:59).
     * 
     * @param startDate Data/hora de início do período (ISO 8601). Ex: 2026-01-01T00:00:00-03:00
     * @param endDate Data/hora de fim do período (ISO 8601). Ex: 2026-01-31T23:59:59-03:00
     * @return Resumo obtido com sucesso
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Resumo completo do dashboard",
        description = "Retorna KPIs, tipos de veículos, bairros e cidades de origem. " +
                      "Se os parâmetros de data não forem informados, assume o dia atual (00:00 até 23:59)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resumo obtido com sucesso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @Parameter(description = "Data/hora de início do período (ISO 8601). Ex: 2026-01-01T00:00:00-03:00")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            
            @Parameter(description = "Data/hora de fim do período (ISO 8601). Ex: 2026-01-31T23:59:59-03:00")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate
    ) {
        DashboardSummaryDTO summary = dashboardService.getSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Retorna KPIs principais do sistema, como total de vagas, 
     * ocupação, reservas ativas/concluídas/canceladas e 
     * reservas de múltiplas vagas.
     * Se os parâmetros de data não forem informados, assume o dia atual (00:00 até 23:59).
     * 
     * @param startDate Data/hora de início do período (ISO 8601). Ex: 2026-01-01T00:00:00-03:00
     * @param endDate Data/hora de fim do período (ISO 8601). Ex: 2026-01-31T23:59:59-03:00
     * @return KPIs obtidos com sucesso
     */
    @GetMapping("/kpis")
    @Operation(
        summary = "KPIs principais do sistema",
        description = "Retorna total de vagas, ocupação, reservas ativas/concluídas/canceladas e reservas de múltiplas vagas. " +
                      "Se os parâmetros de data não forem informados, assume o dia atual (00:00 até 23:59)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KPIs obtidos com sucesso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<DashboardKpiDTO> getKpis(
            @Parameter(description = "Data/hora de início do período (ISO 8601). Ex: 2026-01-01T00:00:00-03:00")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            
            @Parameter(description = "Data/hora de fim do período (ISO 8601). Ex: 2026-01-31T23:59:59-03:00")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate
    ) {
        DashboardKpiDTO kpis = dashboardService.getKpis(startDate, endDate);
        return ResponseEntity.ok(kpis);
    }
}
