package com.cptrans.petrocarga.interfaces.controllers;
// package com.cptrans.petrocarga.controllers;

// TODO: Refatorar EmpresaController

// import java.util.List;
// import java.util.UUID;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.cptrans.petrocarga.dto.EmpresaResponseDTO;
// import com.cptrans.petrocarga.services.EmpresaService;

// @RestController
// @RequestMapping("/empresas")
// public class EmpresaController {

//     @Autowired
//     private EmpresaService empresaService;

//     @GetMapping
//     public ResponseEntity<List<EmpresaResponseDTO>> getAllEmpresas() {
//         List<EmpresaResponseDTO> empresas = empresaService.findAll().stream()
//                 .map(EmpresaResponseDTO::new)
//                 .collect(Collectors.toList());
//         return ResponseEntity.ok(empresas);
//     }

//     // @GetMapping("/{id}")
//     // public ResponseEntity<EmpresaResponseDTO> getEmpresaById(@PathVariable UUID id) {
//     //     return empresaService.findById(id)
//     //             .map(EmpresaResponseDTO::new)
//     //             .map(ResponseEntity::ok)
//     //             .orElse(ResponseEntity.notFound().build());
//     // }

//     // @PostMapping
//     // public ResponseEntity<EmpresaResponseDTO> createEmpresa(@RequestBody @Valid EmpresaRequestDTO empresaRequestDTO) {
//     //     return usuarioService.findById(empresaRequestDTO.getUsuarioId())
//     //             .map(usuario -> {
//     //                 Empresa empresa = empresaRequestDTO.toEntity(usuario);
//     //                 Empresa savedEmpresa = empresaService.save(empresa);
//     //                 return ResponseEntity.status(HttpStatus.CREATED).body(new EmpresaResponseDTO(savedEmpresa));
//     //             })
//     //             .orElse(ResponseEntity.badRequest().build()); // Or a more specific error
//     // }

//     // @PutMapping("/{id}")
//     // public ResponseEntity<EmpresaResponseDTO> updateEmpresa(@PathVariable UUID id, @RequestBody @Valid EmpresaRequestDTO empresaRequestDTO) {
//     //     return empresaService.findById(id)
//     //             .map(existingEmpresa -> {
//     //                 return usuarioService.findById(empresaRequestDTO.getUsuarioId())
//     //                         .map(usuario -> {
//     //                             existingEmpresa.setUsuario(usuario);
//     //                             existingEmpresa.setCnpj(empresaRequestDTO.getCnpj());
//     //                             existingEmpresa.setRazaoSocial(empresaRequestDTO.getRazaoSocial());
//     //                             Empresa updatedEmpresa = empresaService.save(existingEmpresa);
//     //                             return ResponseEntity.ok(new EmpresaResponseDTO(updatedEmpresa));
//     //                         })
//     //                         .orElse(ResponseEntity.badRequest().build());
//     //             })
//     //             .orElse(ResponseEntity.notFound().build());
//     // }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteEmpresa(@PathVariable UUID id) {
//         empresaService.deleteById(id);
//         return ResponseEntity.noContent().build();
//     }
// }