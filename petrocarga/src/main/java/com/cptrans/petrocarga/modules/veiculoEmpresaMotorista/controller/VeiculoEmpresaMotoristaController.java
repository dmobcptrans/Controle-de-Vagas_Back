package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response.VeiculoEmpresaMotoristaResponseDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.service.VeiculoEmpresaMotoristaService;
import com.cptrans.petrocarga.security.UserAuthenticated;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/veiculoEmpresaMotorista")
@RequiredArgsConstructor
public class VeiculoEmpresaMotoristaController {
    private final VeiculoEmpresaMotoristaService service;

    @PreAuthorize("hasRole('ADMIN', 'EMPRESA')")
    @PostMapping
    public ResponseEntity<VeiculoEmpresaMotoristaResponseDTO> vincularMotoristaAoVeiculo(@AuthenticationPrincipal UserAuthenticated usuarioAutenticado, @RequestParam UUID veiculoId, @RequestParam UUID motoristaId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.vincularMotoristaAoVeiculo(usuarioAutenticado, veiculoId, motoristaId));
    }

    @PreAuthorize("hasRole('ADMIN', 'EMPRESA')")
    @PostMapping("/desvincular")
    public ResponseEntity<Void> desvincularMotoristaDoVeiculo(@AuthenticationPrincipal UserAuthenticated usuarioAutenticado, @RequestParam UUID veiculoId, @RequestParam UUID motoristaId) {
        service.desvincularMotoristaDoVeiculo(usuarioAutenticado, veiculoId, motoristaId);
        return ResponseEntity.noContent().build();
    }
    
}
