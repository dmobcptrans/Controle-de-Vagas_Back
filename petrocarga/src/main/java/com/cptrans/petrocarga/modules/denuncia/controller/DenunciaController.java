package com.cptrans.petrocarga.modules.denuncia.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PatchResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.TipoDenunciaEnum;
import com.cptrans.petrocarga.modules.denuncia.dto.mapper.DenunciaMapper;
import com.cptrans.petrocarga.modules.denuncia.dto.request.DenunciaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.request.DenunciaRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.request.FinalizarDenunciaRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.response.DenunciaResponseDTO;
import com.cptrans.petrocarga.modules.denuncia.entity.Denuncia;
import com.cptrans.petrocarga.modules.denuncia.service.DenunciaService;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
public class DenunciaController {
    private final DenunciaService denunciaService;
    private final UsuarioService usuarioService;
    private final DenunciaMapper denunciaMapper;
    
    @Operation(
        summary = "Criar denúncia",
        description = "Realiza o cadastro de uma nova denúncia."
    )
    @PostResponses
    @DefaultResponses
    @PostMapping()
    public ResponseEntity<DenunciaResponseDTO> createDenuncia(
        
        @Parameter(description = "Usuário autenticado") 
        @AuthenticationPrincipal UserAuthenticated userAuthenticated,

        @Parameter(description = "Dados da denúncia")
        @RequestBody @Valid DenunciaRequestDTO denunciaRequest

    ) {
        Denuncia denunciaCriada = denunciaService.create(userAuthenticated, denunciaRequest); 
        return ResponseEntity.status(HttpStatus.CREATED).body(denunciaMapper.toResponse(denunciaCriada));
    }

    @Operation(
        summary = "Retorna todas as denúncias",
        description = "Retorna todas as denúncias de forma paginada e com filtros opicionais."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping("all")
    public ResponseEntity<PageResponseDTO> getAll(

        @Parameter(description = "ID da denúncia")
        @RequestParam(required = false) UUID denunciaId,

        @Parameter(description = "ID da vaga")
        @RequestParam(required = false) UUID vagaId,

        @Parameter(description = "ID da reserva")
        @RequestParam(required = false) UUID reservaId,

        @Parameter(description = "ID do usuário criador da denúncia")
        @RequestParam(required = false) UUID criadoPorId,

        @Parameter(description = "Nome do usuário criador da denúncia")
        @RequestParam(required = false) String criadoPorNome,

        @Parameter(description = "Telefone do usuário criador da denúncia")
        @RequestParam(required = false) String criadoPorTelefone,

        @Parameter(description = "Status da denúncia")
        @RequestParam(required = false) List<StatusDenunciaEnum> listaStatus,
        
        @Parameter(description = "Tipo da denúncia")
        @RequestParam(required = false) List<TipoDenunciaEnum> listaTipos,

        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da listagem", example = "DESC")
        @RequestParam(defaultValue = "DESC") OrdemEnum ordem
    ) {
        DenunciaFiltrosRequestDTO filtros = new DenunciaFiltrosRequestDTO(denunciaId, vagaId, reservaId, criadoPorId, criadoPorNome, criadoPorTelefone, listaStatus, listaTipos);
        return ResponseEntity.ok().body(denunciaService.findAllWithFilters(filtros, pagina, tamanhoPagina, ordem));
    }


    @Operation(
        summary = "Retorna uma denúncia",
        description = "Retorna uma denúncia com base no seu ID."
    )
    @GetResponses
    @DefaultResponses
    @GetMapping("{denunciaId}")
    public ResponseEntity<DenunciaResponseDTO> getDenuncia(
        
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated, 
        
        @Parameter(description = "ID da denúncia")
        @PathVariable UUID denunciaId
    ) {
        Denuncia denuncia = denunciaService.findByIdAutenticado(userAuthenticated, denunciaId);
        return ResponseEntity.ok().body(denunciaMapper.toResponse(denuncia));
    }

    @Operation(
        summary = "Retorna todas as denúncias de um usuário",
        description = "Retorna todas as denúncias de um usuário de forma paginada com base no seu ID e filtros opcionais."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping("byUsuario/{usuarioId}")
    public ResponseEntity<PageResponseDTO> getDenunciasByUsuario(
        
        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId, 

        @Parameter(description = "Lista de status da denúncia")
        @RequestParam(required = false) List<StatusDenunciaEnum> listaStatus,

        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da listagem", example = "DESC")
        @RequestParam(defaultValue = "DESC") OrdemEnum ordem
        
    ) {
        return ResponseEntity.ok().body(denunciaService.findAllByUsuarioIdAndStatusIn(usuarioId, listaStatus, pagina, tamanhoPagina, ordem));
    }

    @Operation(
        summary = "Iniciar a análise de uma denúncia",
        description = "Inicia a análise de uma denúncia com base no seu ID."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR', 'AGENTE')")
    @PatchMapping("iniciarAnalise/{denunciaId}")
    public ResponseEntity<DenunciaResponseDTO> iniciarAnalise(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @PathVariable UUID denunciaId) {
        Usuario usuarioLogado = usuarioService.findByIdAndAtivo(userAuthenticated.id(), true);
        return ResponseEntity.ok().body(denunciaMapper.toResponse(denunciaService.iniciarAnalise(usuarioLogado, denunciaId)));
    }

    @Operation(
        summary = "Finalizar a análise de uma denúncia",
        description = "Finaliza a análise de uma denúncia com base no seu ID."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR', 'AGENTE')")
    @PatchMapping("finalizarAnalise/{denunciaId}")
    public ResponseEntity<DenunciaResponseDTO> finalizarAnalise(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @PathVariable UUID denunciaId, @RequestBody @Valid FinalizarDenunciaRequestDTO respostaRequest) {
        Usuario usuarioLogado = usuarioService.findByIdAndAtivo(userAuthenticated.id(), true);
        return ResponseEntity.ok().body(denunciaMapper.toResponse((denunciaService.finalizarAnalise(usuarioLogado, denunciaId, respostaRequest))));
    }
    
}