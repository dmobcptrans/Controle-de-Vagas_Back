package com.cptrans.petrocarga.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.cptrans.petrocarga.dto.DenunciaRequestDTO;
import com.cptrans.petrocarga.dto.DenunciaResponseDTO;
import com.cptrans.petrocarga.dto.FinalizarDenunciaRequestDTO;
import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.TipoDenunciaEnum;
import com.cptrans.petrocarga.models.Denuncia;
import com.cptrans.petrocarga.models.Reserva;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.services.DenunciaService;
import com.cptrans.petrocarga.services.ReservaService;
import com.cptrans.petrocarga.services.UsuarioService;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/denuncias")
public class DenunciaController {

    @Autowired
    private DenunciaService denunciaService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ReservaService reservaService;
    
/**
 * Cria uma denúncia com base nos dados do request.
 *
 * @param userAuthenticated Usuário autenticado.
 * @param denunciaRequest Dados da denúncia a ser criada.
 * @return Denúncia criada com sucesso.
 */
    @PostMapping()
    public ResponseEntity<DenunciaResponseDTO> createDenuncia(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody @Valid DenunciaRequestDTO denunciaRequest) {
        Usuario usuarioLogado = usuarioService.findByIdAndAtivo(userAuthenticated.id(), true);
        Reserva reserva = reservaService.findById(denunciaRequest.getReservaId());
        Denuncia denunciaCriada = denunciaService.create(denunciaRequest.toEntity(usuarioLogado, reserva.getVaga(), reserva)); 
        return ResponseEntity.status(HttpStatus.CREATED).body(denunciaCriada.toResponseDTO());
    }

/**
 * Retorna todas as denúncias.
 *
 * Se vagaId, listaStatus ou listaTipos forem informados, filtra as denúncias com base nesses parâmetros.
 *
 * Se nenhum dos parâmetros for mencionado, retorna todas as denúncias.
 *
 * @param vagaId ID da vaga a ser filtrada.
 * @param listaStatus Lista de status a ser filtrada.
 * @param listaTipos Lista de tipos a ser filtrada.
 * @return Lista de denúncias filtradas ou todas as denúncias.
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping("all")
    public ResponseEntity<List<DenunciaResponseDTO>> getAll(@RequestParam(required = false) UUID vagaId, @RequestParam(required = false) List<StatusDenunciaEnum> listaStatus, @RequestParam(required = false) List<TipoDenunciaEnum> listaTipos) {
        if(vagaId != null || listaStatus != null || listaTipos != null) {
            return ResponseEntity.ok().body(denunciaService.findAllWithFilters(vagaId, listaStatus, listaTipos).stream().map(DenunciaResponseDTO::new).collect(Collectors.toList()));
        }
        return ResponseEntity.ok().body(denunciaService.findAll().stream().map(DenunciaResponseDTO::new).collect(Collectors.toList()));
    }


/**
 * Retorna uma denúncia com base no seu ID.
 *
 * @param userAuthenticated Usuário autenticado.
 * @param denunciaId ID da denúncia a ser retornada.
 * @return Denúncia com base no seu ID.
 */
    @GetMapping("{denunciaId}")
    public ResponseEntity<DenunciaResponseDTO> getDenuncia(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @PathVariable UUID denunciaId) {
        return ResponseEntity.ok().body(denunciaService.findByIdAutenticado(userAuthenticated, denunciaId).toResponseDTO());
    }
    

/**
 * Retorna todas as denúncias criadas por um usuário com base no seu ID.
 *
 * Se status for informado, filtra as denúncias com base nesse status.
 *
 * Se status for nulo, retorna todas as denúncias criadas pelo usuário.
 *
 * @param usuarioId ID do usuário.
 * @param status Status a ser filtrado.
 * @return Lista de denúncias filtradas ou todas as denúncias criadas pelo usuário.
 */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping("byUsuario/{usuarioId}")
    public ResponseEntity<List<DenunciaResponseDTO>> getDenunciasByUsuario(@PathVariable UUID usuarioId, @RequestParam(required = false) StatusDenunciaEnum status) {
        if(status != null) {
            return ResponseEntity.ok().body(denunciaService.findAllByUsuarioIdAndStatus(usuarioId, status).stream().map(DenunciaResponseDTO::new).collect(Collectors.toList()));
        }
        return ResponseEntity.ok().body(denunciaService.findAllByUsuarioId(usuarioId).stream().map(DenunciaResponseDTO::new).collect(Collectors.toList()));
    }

/**
 * Inicia a análise de uma denúncia.
 * 
 * @param userAuthenticated Usuário autenticado.
 * @param denunciaId ID da denúncia a ser iniciada a análise.
 * @return Denúncia com base no seu ID e status de análise iniciada.
 */
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR', 'AGENTE')")
    @PatchMapping("iniciarAnalise/{denunciaId}")
    public ResponseEntity<DenunciaResponseDTO> iniciarAnalise(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @PathVariable UUID denunciaId) {
        Usuario usuarioLogado = usuarioService.findByIdAndAtivo(userAuthenticated.id(), true);
        return ResponseEntity.ok().body(denunciaService.iniciarAnalise(usuarioLogado, denunciaId).toResponseDTO());
    }

/**
 * Finaliza a análise de uma denúncia.
 *
 * @param userAuthenticated Usuário autenticado.
 * @param denunciaId ID da denúncia a ser finalizada a análise.
 * @param respostaRequest Dados da resposta da denúncia.
 * @return Denúncia com base no seu ID e status de análise finalizada.
 */
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR', 'AGENTE')")
    @PatchMapping("finalizarAnalise/{denunciaId}")
    public ResponseEntity<DenunciaResponseDTO> finalizarAnalise(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @PathVariable UUID denunciaId, @RequestBody @Valid FinalizarDenunciaRequestDTO respostaRequest) {
        Usuario usuarioLogado = usuarioService.findByIdAndAtivo(userAuthenticated.id(), true);

        return ResponseEntity.ok().body(denunciaService.finalizarAnalise(usuarioLogado, denunciaId, respostaRequest).toResponseDTO());
    }
    
}
