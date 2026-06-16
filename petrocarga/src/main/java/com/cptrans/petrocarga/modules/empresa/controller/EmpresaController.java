package com.cptrans.petrocarga.modules.empresa.controller;


import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.modules.empresa.dto.mapper.EmpresaMapper;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.empresa.service.EmpresaService;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping
    public ResponseEntity<PageResponseDTO> getAllEmpresas(
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String razaoSocial,
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanhoPagina,
            @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
        EmpresaFiltrosRequestDTO filtros = new EmpresaFiltrosRequestDTO(empresaId, usuarioId, cnpj, razaoSocial, nome);
        return ResponseEntity.ok(empresaService.listarEmpresas(filtros, pagina, tamanhoPagina, ordem));
    }

    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{usuarioId}")
    public ResponseEntity<EmpresaResponseDTO> getEmpresaByUsuarioId(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(EmpresaMapper.toResponse(empresaService.findByUsuarioId(usuarioId)));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<EmpresaResponseDTO> createEmpresa(@RequestBody @Valid EmpresaRequestDTO empresaRequestDTO) {
        EmpresaResponseDTO response = empresaService.create(empresaRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{usuarioId}")
    public ResponseEntity<EmpresaResponseDTO> updateEmpresa(@PathVariable UUID usuarioId, @RequestBody UsuarioPATCHRequestDTO request) {
        EmpresaResponseDTO response = empresaService.update(usuarioId, request);
        return ResponseEntity.ok(response);
    }
}