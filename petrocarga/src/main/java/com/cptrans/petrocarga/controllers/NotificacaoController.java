package com.cptrans.petrocarga.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.cptrans.petrocarga.dto.PushTokenStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.cptrans.petrocarga.dto.NotificacaoRequestDTO;
import com.cptrans.petrocarga.dto.PushTokenRequestDTO;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.infrastructure.realtime.SseNotficationService;
import com.cptrans.petrocarga.models.Notificacao;
import com.cptrans.petrocarga.models.PushToken;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.services.NotificacaoService;
import com.cptrans.petrocarga.services.PushTokenService;
import com.cptrans.petrocarga.utils.AuthUtils;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;




@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {
    @Autowired
    private SseNotficationService sseNotficationService;
    @Autowired
    private NotificacaoService notificacaoService;
    @Autowired
    private PushTokenService pushTokenService;
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal UserAuthenticated user, HttpServletResponse response) {
        if (user == null) {
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("Acesso negado"));
            } catch (IOException ignored) { }
            emitter.complete();
            return emitter;
        }

        SseEmitter emitter = sseNotficationService.connect(user.id());
        
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        return emitter;
    }

    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("byUsuario/{usuarioId}")
    public ResponseEntity<List<Notificacao>> getAllByUsuarioId(@PathVariable UUID usuarioId, @Schema(example = "false")@RequestParam(required = false) String lida) {
        if (lida != null) {
            if(lida.trim().toLowerCase().equals("true") || lida.trim().toLowerCase().equals("false")){
                boolean lidaBoolean = Boolean.parseBoolean(lida);
                return ResponseEntity.ok().body(notificacaoService.findAllbyUsuarioId(usuarioId, lidaBoolean));
            }
        }
        return ResponseEntity.ok().body(notificacaoService.findAllbyUsuarioId(usuarioId));
        
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @GetMapping("/{id}")
    public ResponseEntity<Notificacao> findByIdAndSetLida(@PathVariable UUID id) {
        return ResponseEntity.ok().body(notificacaoService.findByIdAndSetLida(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @PostMapping("/sendNotification/toUsuario/{usuarioId}")
    public ResponseEntity<Notificacao> sendNotificationToUsuario(@PathVariable UUID usuarioId, @Valid @RequestBody NotificacaoRequestDTO notificacaoRequestDTO) {
        Notificacao notificacaoEnviada = notificacaoService.sendNotificationToUsuario(usuarioId, notificacaoRequestDTO.toEntity());
        return ResponseEntity.ok().body(notificacaoEnviada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE')")
    @PostMapping("/sendNotification/byPermissao/{permissao}")
    public ResponseEntity<List<Notificacao>> sendNotificationToPermissao(@PathVariable PermissaoEnum permissao, @Valid @RequestBody NotificacaoRequestDTO notificacaoRequestDTO) {
        List<Notificacao> notificacoesEnviadas = notificacaoService.sendNotificacaoToUsuariosByPermissao(permissao, notificacaoRequestDTO.toEntity());
        return ResponseEntity.ok().body(notificacoesEnviadas);
    }

    @PatchMapping("/lida/{notificacaoId}")
    public ResponseEntity<Notificacao> marcarComoLida(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @PathVariable UUID notificacaoId) {
        UUID usuarioId = userAuthenticated.id();
        AuthUtils.validarPemissoesUsuarioLogado(userAuthenticated, usuarioId, List.of(PermissaoEnum.ADMIN.getRole()));
        Notificacao notificacaoLida = notificacaoService.marcarComoLida(usuarioId, notificacaoId);
        return ResponseEntity.ok().body(notificacaoLida);
    }

    @PreAuthorize("#usuarioId == authentication.principal.id")
    @PatchMapping("/marcarSelecionadasComoLida/{usuarioId}")
    public ResponseEntity<List<Notificacao>> marcarSelecionadasComoLida(@PathVariable UUID usuarioId, @AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestParam(required = true) List<UUID> listaNotificacaoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacaoService.marcarSelecionadasComoLida(usuarioId, listaNotificacaoId));
    }

    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{usuarioId}/{notificacaoId}")
    public ResponseEntity<Void> deleteNotificacao(@PathVariable UUID usuarioId, @PathVariable UUID notificacaoId) {
        notificacaoService.deleteById(notificacaoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("deletarSelecionadas/{usuarioId}")
    public ResponseEntity<Void> deleteNotificacoesSelecionadas(@PathVariable UUID usuarioId, @RequestParam(required = true) List<UUID> listaNotificacaoId) {
        notificacaoService.deletarSelecionadas(usuarioId, listaNotificacaoId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @PostMapping("/pushToken")
    public ResponseEntity<Map<String, String>> registrarToken( @AuthenticationPrincipal UserAuthenticated userAuthenticated, @Valid @RequestBody PushTokenRequestDTO pushTokenRequestDTO) {
        PushToken novaPushToken = pushTokenRequestDTO.toEntity();
        novaPushToken.setUsuarioId(userAuthenticated.id());
        pushTokenService.salvar(novaPushToken);
        return ResponseEntity.ok().body(Map.of("message", "Token registrado com sucesso!"));
    }

    @PreAuthorize("#usuarioId == authentication.principal.id")
    @PatchMapping("/pushToken/{usuarioId}")
    public ResponseEntity<Map<String, String>> atualizarStatus(@PathVariable UUID usuarioId, @AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody PushTokenStatusDTO pushTokenStatusDTO) {
        pushTokenService.atualizarStatus(usuarioId, pushTokenStatusDTO.getAtivo());
        return ResponseEntity.ok().body(Map.of("message", "Status do token atualizado com sucesso!"));
    }

}