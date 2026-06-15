package com.cptrans.petrocarga.modules.empresa.controller;


import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.empresa.service.EmpresaService;
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

    // @GetMapping("/{id}")
    // public ResponseEntity<EmpresaResponseDTO> getEmpresaById(@PathVariable UUID id) {
    //     return empresaService.findById(id)
    //             .map(EmpresaResponseDTO::new)
    //             .map(ResponseEntity::ok)
    //             .orElse(ResponseEntity.notFound().build());
    // }

    @PostMapping("/cadastro")
    public ResponseEntity<EmpresaResponseDTO> createEmpresa(@RequestBody @Valid EmpresaRequestDTO empresaRequestDTO) {
        EmpresaResponseDTO response = empresaService.create(empresaRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // @PutMapping("/{id}")
    // public ResponseEntity<EmpresaResponseDTO> updateEmpresa(@PathVariable UUID id, @RequestBody @Valid EmpresaRequestDTO empresaRequestDTO) {
    //     return empresaService.findById(id)
    //             .map(existingEmpresa -> {
    //                 return usuarioService.findById(empresaRequestDTO.getUsuarioId())
    //                         .map(usuario -> {
    //                             existingEmpresa.setUsuario(usuario);
    //                             existingEmpresa.setCnpj(empresaRequestDTO.getCnpj());
    //                             existingEmpresa.setRazaoSocial(empresaRequestDTO.getRazaoSocial());
    //                             Empresa updatedEmpresa = empresaService.save(existingEmpresa);
    //                             return ResponseEntity.ok(new EmpresaResponseDTO(updatedEmpresa));
    //                         })
    //                         .orElse(ResponseEntity.badRequest().build());
    //             })
    //             .orElse(ResponseEntity.notFound().build());
    // }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteEmpresa(@PathVariable UUID id) {
    //     empresaService.deleteById(id);
    //     return ResponseEntity.noContent().build();
    // }
}