package com.cptrans.petrocarga.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.cptrans.petrocarga.dto.PushTokenResponseDTO;
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
import com.cptrans.petrocarga.dto.PushTokenPatchDTO;
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
    
/*************  ✨ Windsurf Command ⭐  *************/
/**
 * Esse método é responsável por criar uma conexão SSE (Server-Sent Events) para o usuário autenticado, permitindo que ele receba notificações em tempo real.
 * @param User o usuário autenticado
 * @return SseEmitter o SseEmitter
 * @throws IOException se o SseEmitter falhar 
 */
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

    /**
     * Retorna todas as notificações de um usuário.
     * Pode ser filtrado por notificações lidas ou não lidas.
     * Apenas o usuário autenticado e usuários com permissão de ADMIN ou GESTOR
     * podem acessar esse método.
     * 
     * @param usuarioId o id do usuário
     * @param lida se a notificação é lida ou não
     * @return lista de notificações do usuário filtradas (se o parâmetro lida for passado) ou todas as notificações do usuário (se o parâmetro lida não for passado).
     */
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

    /**
     * Retorna uma notificação com base no seu id e marca como lida.
     * Só permite que a notificação seja acessada por um usuário com permissão de ADMIN, GESTOR, AGENTE, EMPRESA ou MOTORISTA.
     * 
     * @param id o id da notificação
     * @return a notificação com base no seu id e status de leitura
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @GetMapping("/{id}")
    public ResponseEntity<Notificacao> findByIdAndSetLida(@PathVariable UUID id) {
        return ResponseEntity.ok().body(notificacaoService.findByIdAndSetLida(id));
    }
/**
 * Envia uma notificação para um usuário com base no seu id.
 * Só permite que notificação seja enviada por um usuário com permissão de ADMIN ou GESTOR.
 * @param usuarioId o id do usuário que receberá a notificação
 * @param notificacaoRequestDTO o corpo da notificação a ser enviada
 * @return a notificação que foi enviada.
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/sendNotification/toUsuario/{usuarioId}")
    public ResponseEntity<Notificacao> sendNotificationToUsuario(@PathVariable UUID usuarioId, @Valid @RequestBody NotificacaoRequestDTO notificacaoRequestDTO) {
        Notificacao notificacaoEnviada = notificacaoService.sendNotificationToUsuario(usuarioId, notificacaoRequestDTO.toEntity());
        return ResponseEntity.ok().body(notificacaoEnviada);
    }


/**
 * Envia uma notificação para todos os usuários baseado na permissão.
 * 
 * Só permite que notificação seja enviada por um usuário com permissão de ADMIN ou GESTOR.
 * 
 * @param permissao a permissão dos usuários que receberão a notificação
 * @param notificacaoRequestDTO o corpo da notificação a ser enviada
 * @return a lista de notificações que foram enviadas
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/sendNotification/byPermissao/{permissao}")
    public ResponseEntity<List<Notificacao>> sendNotificationToPermissao(@PathVariable PermissaoEnum permissao, @Valid @RequestBody NotificacaoRequestDTO notificacaoRequestDTO) {
        List<Notificacao> notificacoesEnviadas = notificacaoService.sendNotificacaoToUsuariosByPermissao(permissao, notificacaoRequestDTO.toEntity());
        return ResponseEntity.ok().body(notificacoesEnviadas);
    }

/**
 * Marca uma notificação como lida.
 * Só o próprio usuário dono da notificação ou um usuário com permissão de ADMIN pode marcar a notificação como lida.
 * @param userAuthenticated o usuário autenticado.
 * @param notificacaoId o id da notificação a ser marcada como lida.
 * @return a notificação que foi marcada como lida.
 * 
 */
    @PatchMapping("/lida/{notificacaoId}")
    public ResponseEntity<Notificacao> marcarComoLida(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @PathVariable UUID notificacaoId) {
        UUID usuarioId = userAuthenticated.id();
        AuthUtils.validarPemissoesUsuarioLogado(userAuthenticated, usuarioId, List.of(PermissaoEnum.ADMIN.getRole()));
        Notificacao notificacaoLida = notificacaoService.marcarComoLida(usuarioId, notificacaoId);
        return ResponseEntity.ok().body(notificacaoLida);
    }

/**
 * Marca as notificações com base no id do usuário e lista de ids de notificações como lidas.

 * Só  permite que as notificações sejam marcadas como lidas pelo próprio dono ou por um usuário com permissão de ADMIN
 * 
 * @param usuarioId o id do usuário
 * @param userAuthenticated o usuário autenticado
 * @param listaNotificacaoId a lista de ids de notificações a ser marcadas como lidas
 * @return a lista de notificações que foram marcadas como lidas
 */
    @PreAuthorize("#usuarioId == authentication.principal.id")
    @PatchMapping("/marcarSelecionadasComoLida/{usuarioId}")
    public ResponseEntity<List<Notificacao>> marcarSelecionadasComoLida(@PathVariable UUID usuarioId, @AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestParam(required = true) List<UUID> listaNotificacaoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacaoService.marcarSelecionadasComoLida(usuarioId, listaNotificacaoId));
    }

    /**
     * Deleta uma notificação com base no id do usuário e no id da notificação.
     * Só  permite que a notificação seja deletada pelo próprio dono ou por um usuário com permissão de ADMIN ou GESTOR.
     * @param usuarioId o id do usuário
     * @param notificacaoId o id da notificação a ser deletada
     * @return vazio, pois a notificação foi deletada
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{usuarioId}/{notificacaoId}")
    public ResponseEntity<Void> deleteNotificacao(@PathVariable UUID usuarioId, @PathVariable UUID notificacaoId) {
        notificacaoService.deleteById(notificacaoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deleta as notificações com base no id do usuário e lista de ids de notificações.
     * Só  permite que as notificações sejam deletadas pelo próprio dono ou por um usuário com permissão de ADMIN ou GESTOR.
     * 
     * @param usuarioId o id do usuário
     * @param listaNotificacaoId a lista de ids de notificações a ser deletadas
     * @return vazio, pois as notificações foram deletadas
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("deletarSelecionadas/{usuarioId}")
    public ResponseEntity<Void> deleteNotificacoesSelecionadas(@PathVariable UUID usuarioId, @RequestParam(required = true) List<UUID> listaNotificacaoId) {
        notificacaoService.deletarSelecionadas(usuarioId, listaNotificacaoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Registra um token de push para o usuário autenticado. Só permite que o token seja registrado por um usuário autenticado com permissão de ADMIN, GESTOR, AGENTE, EMPRESA ou MOTORISTA.
     * @param userAuthenticated o usuário autenticado
     * @param pushTokenRequestDTO o objeto com os dados do token a ser registrado
     * @return uma resposta contendo a mensagem de sucesso
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @PostMapping("/pushToken")
    public ResponseEntity<Map<String, String>> registrarToken( @AuthenticationPrincipal UserAuthenticated userAuthenticated, @Valid @RequestBody PushTokenRequestDTO pushTokenRequestDTO) {
        PushToken novaPushToken = pushTokenRequestDTO.toEntity();
        novaPushToken.setUsuarioId(userAuthenticated.id());
        pushTokenService.salvar(novaPushToken);
        return ResponseEntity.ok().body(Map.of("message", "Token registrado com sucesso!"));
    }

/**
 * Visualiza o status de um token de push com base no token e no id do usuário.
 * Só permite que o status do token seja visualizado por um usuário autenticado com permissão de ADMIN, GESTOR, AGENTE, EMPRESA ou MOTORISTA.
 * @param userAuthenticated o usuário autenticado
 * @param token o token a ser visualizado
 * @return o token a ser visualizado
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @GetMapping("/pushToken/byToken")
    public ResponseEntity<PushTokenResponseDTO> visualizarStatusByTokenAndUsuario(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestParam(required = true) String token) {
        PushToken pushToken = pushTokenService.visualizarStatusByTokenAndUsuario(token, userAuthenticated.id());
        return ResponseEntity.ok(pushToken.toResponseDTO());
    }

/**
 * Visualiza o status de todos os tokens de push de um usuário com base no id do usuário.
 * Só permite que o status do token seja visualizado por um usuário autenticado com permissão de ADMIN, GESTOR, AGENTE, EMPRESA ou MOTORISTA.
 * @param userAuthenticated o usuário autenticado
 * @return a lista de tokens a serem visualizados
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @GetMapping("/pushToken/byUsuarioId")
    public ResponseEntity<List<PushTokenResponseDTO>> visualizarStatusByUsuario(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        List<PushToken> pushTokenList = pushTokenService.visualizarStatusByUsuario(userAuthenticated.id());
        return ResponseEntity.ok(pushTokenList.stream().map(pushToken -> pushToken.toResponseDTO()).toList());
    }

    /**
     * Atualiza o status de um token de push com base no token e no id do usuario.
     * Só permite que o status do token seja atualizado por um usuário autenticado com permissão de ADMIN, GESTOR, AGENTE, EMPRESA ou MOTORISTA.
     * @param usuarioId o id do usuário
     * @param request o corpo da requisição com o token a ser atualizado e o status do token a ser atualizado
     * @return o token a ser atualizado
     */
    @PreAuthorize("#usuarioId == authentication.principal.id")
    @PatchMapping("/pushToken/{usuarioId}")
    public ResponseEntity<PushTokenResponseDTO> atualizarStatus(@PathVariable UUID usuarioId, @AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody PushTokenPatchDTO request) {
        PushToken tokenAtualizado = pushTokenService.atualizarStatus(usuarioId,request.token(), request.ativo());
        return ResponseEntity.ok().body(tokenAtualizado.toResponseDTO());
    }
}