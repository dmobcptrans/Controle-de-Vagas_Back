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

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.reservaRapida.dto.mapper.ReservaRapidaMapper;
import com.cptrans.petrocarga.modules.reservaRapida.dto.request.ReservaRapidaRequestDTO;
import com.cptrans.petrocarga.modules.reservaRapida.dto.response.ReservaRapidaResponseDTO;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.reservaRapida.service.ReservaRapidaService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/reserva-rapida")
@RequiredArgsConstructor
public class ReservaRapidaController {
    private final ReservaRapidaService reservaRapidaService;
    private final ReservaRapidaMapper reservaRapidaMapper;

    /**
     * Cria uma nova reserva rápida por um AGENTE/ADMIN.
     * Só permite que as reservas sejam criadas por um usuário com permissão de ADMIN ou AGENTE.
     * 
     * @param reservaRapidaRequestDTO os dados da reserva a ser criada
     * @return a reserva criada com status CREATED
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @PostMapping()
    public ResponseEntity<ReservaRapidaResponseDTO> createReservaRapida(@RequestBody ReservaRapidaRequestDTO request) {
        ReservaRapida novaReservaRapida = reservaRapidaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaRapidaMapper.toResponse(novaReservaRapida));
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
    @PreAuthorize("#agenteId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{agenteId}")
    public ResponseEntity<PageResponseDTO> getReservasRapidasByagenteId(@PathVariable UUID agenteId, @RequestParam(required = false) UUID vagaId, @RequestParam(required = false) String placaVeiculo, @RequestParam(required = false) LocalDate data, @RequestParam(required = false) List<StatusReservaEnum> listaStatus, @RequestParam(required = false) Integer mes, @RequestParam(required = false) Integer ano, @RequestParam(defaultValue = "0") Integer numeroPagina, @RequestParam(defaultValue = "10") Integer tamanhoPagina) {
        ReservaUtils.validarFiltrosData(data,mes, ano);

        placaVeiculo = placaVeiculo != null ? placaVeiculo.trim().toUpperCase() : null;
        PageResponseDTO reservasRapidas = reservaRapidaService.findByAgenteIdWithFilters(agenteId, vagaId, placaVeiculo, data, listaStatus, mes, ano, numeroPagina, tamanhoPagina);
        return ResponseEntity.ok().body(reservasRapidas);
    }
    
}