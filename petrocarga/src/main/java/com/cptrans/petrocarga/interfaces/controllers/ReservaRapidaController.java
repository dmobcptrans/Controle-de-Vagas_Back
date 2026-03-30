package com.cptrans.petrocarga.interfaces.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.cptrans.petrocarga.application.dto.ReservaRapidaRequestDTO;
import com.cptrans.petrocarga.application.dto.ReservaRapidaResponseDTO;
import com.cptrans.petrocarga.application.usecase.AgenteService;
import com.cptrans.petrocarga.application.usecase.ReservaRapidaService;
import com.cptrans.petrocarga.application.usecase.VagaService;
import com.cptrans.petrocarga.domain.entities.Agente;
import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;


@RestController
@RequestMapping("/reserva-rapida")
public class ReservaRapidaController {
    
    @Autowired
    private ReservaRapidaService reservaRapidaService;
    @Autowired
    private VagaService vagaService;
    @Autowired
    private AgenteService agenteService;

    /**
     * Cria uma nova reserva rápida por um AGENTE/ADMIN.
     * Só permite que as reservas sejam criadas por um usuário com permissão de ADMIN ou AGENTE.
     * 
     * @param reservaRapidaRequestDTO os dados da reserva a ser criada
     * @return a reserva criada com status CREATED
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @PostMapping()
    public ResponseEntity<ReservaRapidaResponseDTO> createReservaRapida(@RequestBody ReservaRapidaRequestDTO reservaRapidaRequest) {
        Vaga vaga = vagaService.findById(reservaRapidaRequest.getVagaId());
        ReservaRapida novaReservaRapida = reservaRapidaService.create(reservaRapidaRequest.toEntity(vaga));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(novaReservaRapida.toResponse());
    }
    
    /**
     * Retorna todas as reservas rapidas dado um usuarioId, com filtros opcionais de vagaId, placaVeiculo, data e listaStatus.
     * Só permite que as reservas sejam acessadas pelo própio dono (agente que criou) ou por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * @param usuarioId o id do usuário para buscar as reservas rápidas
     * @param vagaID o id da vaga para filtrar as reservas
     * @param placaVeiculo a placa do veículo para filtrar as reservas
     * @param data a data da reserva para filtrar as reservas
     * @param listaStatus a lista de status para filtrar as reservas
     * @return A lista de reservas rápidas encontradas com status ok
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<ReservaRapidaResponseDTO>> getReservasRapidasByUsuarioId(@PathVariable UUID usuarioId, @RequestParam(required = false) UUID vagaId, @RequestParam(required = false) String placaVeiculo, @RequestParam(required = false) LocalDate data, @RequestParam(required = false) List<StatusReservaEnum> listaStatus, @RequestParam(defaultValue = "0") Integer numeroPagina, @RequestParam(defaultValue = "10") Integer tamanhoPagina) {
        Agente agente = agenteService.findByUsuarioId(usuarioId);
        if(vagaId != null || placaVeiculo != null || data != null || (listaStatus != null && !listaStatus.isEmpty())) {
            placaVeiculo = placaVeiculo != null ? placaVeiculo.trim().toUpperCase() : null;
            return ResponseEntity.ok().body(reservaRapidaService.findByAgenteWithFilters(agente, vagaId, placaVeiculo, data, listaStatus, numeroPagina, tamanhoPagina).stream().map(ReservaRapida::toResponse).collect(Collectors.toList()));
        }
        List<ReservaRapidaResponseDTO> reservasRapidas = reservaRapidaService.findByAgente(agente.getId(), numeroPagina, tamanhoPagina).stream()
                .map(ReservaRapida::toResponse)
                .toList();
        return ResponseEntity.ok(reservasRapidas);
    }
    
}