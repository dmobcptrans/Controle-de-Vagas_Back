package com.cptrans.petrocarga.interfaces.controllers;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.cptrans.petrocarga.application.dto.DisponibilidadeVagaRequestDTO;
import com.cptrans.petrocarga.application.dto.DisponibilidadeVagaResponseDTO;
import com.cptrans.petrocarga.application.dto.MultiplasDisponibilidadesVagaRequestDTO;
import com.cptrans.petrocarga.application.usecase.DisponibilidadeVagaService;
import com.cptrans.petrocarga.domain.entities.DisponibilidadeVaga;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/disponibilidade-vagas")
public class DisponibilidadeVagaController {

    @Autowired
    private DisponibilidadeVagaService disponibilidadeVagaService;

    /**
     * Retorna uma lista de todas as DisponibilidadeVaga.
     * 
     * @return All DisponibilidadeVaga records.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping
    public ResponseEntity<List<DisponibilidadeVagaResponseDTO>> getAllDisponibilidadeVagas(@RequestParam(required = false) Integer mes, @RequestParam(required = false) Integer ano) {
        if(ano != null || mes != null) {
            if(mes == null) throw new IllegalArgumentException("Ao informar o ano, o mês também deve ser informado.");
            if(ano == null) throw new IllegalArgumentException("Ao informar o mês, o ano também deve ser informado.");
            DateUtils.validarMesEAno(mes, ano);
            List<DisponibilidadeVagaResponseDTO> disponibilidadeVagas = disponibilidadeVagaService.findByMes(mes, ano).stream()
                    .map(DisponibilidadeVagaResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(disponibilidadeVagas);
        }
        DateUtils.validarMesEAno(mes, ano);
        List<DisponibilidadeVagaResponseDTO> disponibilidadeVagas = disponibilidadeVagaService.findAll().stream()
                .map(DisponibilidadeVagaResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(disponibilidadeVagas);
    }

/**
 * Retorna a DisponibilidadeVaga por ID.
 * 
 * @param id DisponibilidadeVaga Id.
 * @return DisponibilidadeVaga por Id.
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/{id}")
    public ResponseEntity<DisponibilidadeVagaResponseDTO> getDisponibilidadeVagaById(@PathVariable UUID id) {
        DisponibilidadeVaga disponibilidadeVaga = disponibilidadeVagaService.findById(id);
        return ResponseEntity.ok(disponibilidadeVaga.toResponseDTO());
    }

/**
 * Retorna uma lista de todas as DisponibilidadeVaga por vagaId.
 * 
 * @param vagaId Vaga Id.
 * @return Lista de todas as DisponibilityVaga registradas com base no vagaId.
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/vaga/{vagaId}")
    public ResponseEntity<List<DisponibilidadeVagaResponseDTO>> getDisponibilidadeVagaByVagaId(@PathVariable UUID vagaId) {
        List<DisponibilidadeVagaResponseDTO> disponibilidadeVaga = disponibilidadeVagaService.findByVagaId(vagaId).stream().map(DisponibilidadeVagaResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(disponibilidadeVaga);
    }

/**
 * Cria uma nova DisponibilidadeVaga com base em um objeto DisponibilidadeVagaRequestDTO.
 * 
 * @param disponibilidadeVagaRequestDTO objeto DisponibilidadeVagaRequestDTO com todas as informações necessárias para criar uma nova DisponibilidadeVaga.
 * @return A DisponibilidadeVaga criada com sucesso.
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<DisponibilidadeVagaResponseDTO> createDisponibilidadeVaga(@RequestBody @Valid DisponibilidadeVagaRequestDTO disponibilidadeVagaRequestDTO) {
        DisponibilidadeVaga savedDisponibilidadeVaga = disponibilidadeVagaService.createDisponibilidadeVaga(disponibilidadeVagaRequestDTO.toEntity(), disponibilidadeVagaRequestDTO.getVagaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDisponibilidadeVaga.toResponseDTO());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/vagas")
    public ResponseEntity<List<DisponibilidadeVagaResponseDTO>> createMultipleDisponibilidadeVagas(@RequestBody @Valid MultiplasDisponibilidadesVagaRequestDTO multiplasDisponibilidadesVagaRequestDTO) {
        List<DisponibilidadeVaga> savedDisponibilidadeVagas = disponibilidadeVagaService.createMultipleDisponibilidadeVagas(multiplasDisponibilidadesVagaRequestDTO.toEntity(), multiplasDisponibilidadesVagaRequestDTO.getListaVagaId());
        List<DisponibilidadeVagaResponseDTO> response = savedDisponibilidadeVagas.stream()
                .map(DisponibilidadeVaga::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<DisponibilidadeVagaResponseDTO> updateDisponibilidadeVaga(@PathVariable UUID id, @RequestBody  DisponibilidadeVagaRequestDTO disponibilidadeVagaRequestDTO) {
        DisponibilidadeVaga disponibilidadeVaga = disponibilidadeVagaService.updateDisponibilidadeVaga(id, disponibilidadeVagaRequestDTO);
        disponibilidadeVagaService.save(disponibilidadeVaga);
        return ResponseEntity.ok(disponibilidadeVaga.toResponseDTO());
      
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/byCodigoPmp")
    public ResponseEntity<List<DisponibilidadeVagaResponseDTO>> updateDisponibilidadeVagaByCodigoPmp(@RequestBody  DisponibilidadeVagaRequestDTO disponibilidadeVagaRequestDTO, @RequestParam(required = true) String codigoPmp) {
        List<DisponibilidadeVaga> disponibilidadeVaga = disponibilidadeVagaService.updateDisponibilidadeVagaByCodigoPmp(disponibilidadeVagaRequestDTO, codigoPmp);
        return ResponseEntity.ok(disponibilidadeVaga.stream().map(DisponibilidadeVaga::toResponseDTO).collect(Collectors.toList()));
      
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/byList")
    public ResponseEntity<List<DisponibilidadeVagaResponseDTO>> updateDisponibilidadeVagaByList(@RequestBody  DisponibilidadeVagaRequestDTO disponibilidadeVagaRequestDTO, @RequestParam(required = true) List<UUID> listaIds) {
        List<DisponibilidadeVaga> disponibilidadeVaga = disponibilidadeVagaService.updateDisponibilidadeVagaByList(disponibilidadeVagaRequestDTO, listaIds);
        return ResponseEntity.ok(disponibilidadeVaga.stream().map(DisponibilidadeVaga::toResponseDTO).collect(Collectors.toList()));
      
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisponibilidadeVaga(@PathVariable UUID id) {
        disponibilidadeVagaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/byList")
    public ResponseEntity<Void> deleteMultiplasDisponibilidadeVaga(@RequestParam(required = true) List<UUID> listaIds) {
        disponibilidadeVagaService.deleteByIdList(listaIds);
        return ResponseEntity.noContent().build();
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/byCodigoPMP")
    public ResponseEntity<Void> deleteMultiplasDisponibilidadeVaga(@RequestParam String codigoPMP) {
        disponibilidadeVagaService.deleteByCodigoPMP(codigoPMP);
        return ResponseEntity.noContent().build();
    }

}