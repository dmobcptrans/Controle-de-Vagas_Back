package com.cptrans.petrocarga.modules.notificacao.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
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

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.DeleteResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PatchResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.messaging.realtime.SseNotficationService;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.auth.utils.AuthUtils;
import com.cptrans.petrocarga.modules.notificacao.dto.request.NotificacaoRequestDTO;
import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;
import com.cptrans.petrocarga.modules.notificacao.service.NotificacaoService;
import com.cptrans.petrocarga.modules.pushToken.dto.mapper.PushTokenMapper;
import com.cptrans.petrocarga.modules.pushToken.dto.request.PushTokenPatchDTO;
import com.cptrans.petrocarga.modules.pushToken.dto.request.PushTokenRequestDTO;
import com.cptrans.petrocarga.modules.pushToken.dto.response.PushTokenResponseDTO;
import com.cptrans.petrocarga.modules.pushToken.entity.PushToken;
import com.cptrans.petrocarga.modules.pushToken.service.PushTokenService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@Tag(name = "Notificação", description = "Endpoints para gerenciamento de notificações")
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {
    private final SseNotficationService sseNotficationService;
    private final NotificacaoService notificacaoService;
    private final PushTokenService pushTokenService;
    private final PushTokenMapper pushTokenMapper;
    
    // GET /notificacoes/stream
    @Operation(
        summary = "Iniciar conexao para receber notificacoes em tempo real",
        description = "Retorna uma conexao SSE para receber notificacoes em tempo real"
    )
    @GetResponses
    @DefaultResponses
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated user,

        @Parameter(description = "Resposta HTTP")
        HttpServletResponse response
    ) {
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

    // GET /notificacoes/byUsuario/{usuarioId}
    @Operation(
        summary = "Retorna todas as notificações de um usuário",
        description = "Retorna todas as notificações de um usuário de forma paginada com base no seu ID"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("byUsuario/{usuarioId}")
    public ResponseEntity<PageResponseDTO> getAllByUsuarioId(
        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId,

        @Parameter(description = "Status da notificação")
        @RequestParam(required = false) Boolean lida,
        
        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") int numeroPagina, 
        
        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina
    ) {
        if (lida != null) {
            Page<Notificacao> page = notificacaoService.findAllbyUsuarioIdAndLida(usuarioId, lida, numeroPagina, tamanhoPagina);
            return ResponseEntity.ok().body(new PageResponseDTO(page));
        }
        Page<Notificacao> page = notificacaoService.findAllbyUsuarioId(usuarioId, numeroPagina, tamanhoPagina);
        return ResponseEntity.ok().body(new PageResponseDTO(page));
        
    }

    // GET /notificacoes/{id}
    @Operation(
        summary = "Retorna uma notificação",
        description = "Retorna uma notificação a partir do seu ID"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @GetMapping("/{id}")
    public ResponseEntity<Notificacao> findByIdAndSetLida(
        @Parameter(description = "ID da notificação")
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok().body(notificacaoService.findByIdAndSetLida(id));
    }

    // POST /notificacoes/sendNotification/toUsuario/{usuarioId}
    @Operation(
        summary = "Envia uma notificação para um usuário",
        description = "Envia uma notificação para um usuário a partir do seu ID"
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/sendNotification/toUsuario/{usuarioId}")
    public ResponseEntity<Notificacao> sendNotificationToUsuario(
        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId, 
        
        @Parameter(description = "Notificação a ser enviada")
        @Valid @RequestBody NotificacaoRequestDTO notificacaoRequestDTO
    ) {
        Notificacao notificacaoEnviada = notificacaoService.sendNotificationToUsuario(usuarioId, notificacaoRequestDTO.toEntity());
        return ResponseEntity.ok().body(notificacaoEnviada);
    }


    // POST /notificacoes/sendNotification/byPermissao/{permissao}
    @Operation(
        summary = "Envia uma notificação por permissão",
        description = "Envia uma notificação para todos os usuários ativos com base na permissão"
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping("/sendNotification/byPermissao/{permissao}")
    public ResponseEntity<List<Notificacao>> sendNotificationToPermissao(
        @Parameter(description = "Permissão")
        @PathVariable PermissaoEnum permissao, 
        
        @Parameter(description = "Notificação a ser enviada")
        @Valid @RequestBody NotificacaoRequestDTO notificacaoRequestDTO
    ) {
        List<Notificacao> notificacoesEnviadas = notificacaoService.sendNotificacaoToUsuariosByPermissao(permissao, notificacaoRequestDTO.toEntity());
        return ResponseEntity.ok().body(notificacoesEnviadas);
    }

    // PATCH /notificacoes/lida/{notificacaoId}
    @Operation(
        summary = "Marcar uma notificação como lida",
        description = "Marca uma notificação como lida a partir do seu ID"
    )
    @PatchResponses
    @DefaultResponses
    @PatchMapping("/lida/{notificacaoId}")
    public ResponseEntity<Notificacao> marcarComoLida(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated, 

        @Parameter(description = "ID da notificação")
        @PathVariable UUID notificacaoId
    ) {
        UUID usuarioId = userAuthenticated.id();
        AuthUtils.validarPemissoesUsuarioLogado(userAuthenticated, usuarioId, List.of(PermissaoEnum.ADMIN.getRole()));
        Notificacao notificacaoLida = notificacaoService.marcarComoLida(usuarioId, notificacaoId);
        return ResponseEntity.ok().body(notificacaoLida);
    }

    // PATCH /notificacoes/marcarSelecionadasComoLida/{usuarioId}
    @Operation(
        summary = "Marcar várias notificações como lida",
        description = "Marca várias notificações de um usuário como lidas a partir do ID de usuário e de uma lista de IDs de notificação"
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id")
    @PatchMapping("/marcarSelecionadasComoLida/{usuarioId}")
    public ResponseEntity<List<Notificacao>> marcarSelecionadasComoLida(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated, 

        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId, 
        
        @Parameter(description = "Lista de IDs de notificação")
        @RequestParam(required = true) List<UUID> listaNotificacaoId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacaoService.marcarSelecionadasComoLida(usuarioId, listaNotificacaoId));
    }

    // DELETE /notificacoes/{usuarioId}/{notificacaoId}
    @Operation(
        summary = "Deletar uma notificação",
        description = "Deletar a notificação de um usuário a partir do ID de usuário e do ID da notificação"
    )
    @DeleteResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{usuarioId}/{notificacaoId}")
    public ResponseEntity<Void> deleteNotificacao(
        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId, 

        @Parameter(description = "ID da notificação")
        @PathVariable UUID notificacaoId
    ) {
        notificacaoService.deleteById(notificacaoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /notificacoes/deletarSelecionadas/{usuarioId}
    @Operation(
        summary = "Deletar várias notificações",
        description = "Deletar vários notificação de um usuário a partir do ID de usuário e de uma lista de IDs de notificação"
    )
    @DeleteResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("deletarSelecionadas/{usuarioId}")
    public ResponseEntity<Void> deleteNotificacoesSelecionadas(
        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId, 
        
        @Parameter(description = "Lista de IDs de notificação")
        @RequestParam(required = true) List<UUID> listaNotificacaoId
    ) {
        notificacaoService.deletarSelecionadas(usuarioId, listaNotificacaoId);
        return ResponseEntity.noContent().build();
    }

    // POST /notificacoes/pushToken
    @Operation(
        summary = "Registrar um push token",
        description = "Registra um push token para o usuário autenticado"
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @PostMapping("/pushToken")
    public ResponseEntity<Map<String, String>> registrarToken( 
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated, 
        
        @Parameter(description = "Informações do push token")
        @Valid @RequestBody PushTokenRequestDTO pushTokenRequestDTO
    ) {
        pushTokenService.registrarToken(pushTokenRequestDTO, userAuthenticated.id());
        return ResponseEntity.ok().body(Map.of("message", "Token registrado com sucesso!"));
    }

    // GET /notificacoes/pushToken/byToken
    @Operation(
        summary = "Visualizar um push token",
        description = "Retorna um push token de um usuário com base no token e no id do usuário"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @GetMapping("/pushToken/byToken")
    public ResponseEntity<PushTokenResponseDTO> visualizarStatusByTokenAndUsuario(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated, 
        
        @Parameter(description = "Push token")
        @RequestParam(required = true) String token
    ) {
        PushToken pushToken = pushTokenService.findByTokenAndUsuarioId(token, userAuthenticated.id());
        return ResponseEntity.ok(pushTokenMapper.toResponse(pushToken));
    }

    // GET /notificacoes/pushToken/byUsuarioId
    @Operation(
        summary = "Visualizar push tokens de um usuário",
        description = "Retorna uma lista de push tokens associados a um usuário com base no id do usuário"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'EMPRESA', 'MOTORISTA')")
    @GetMapping("/pushToken/byUsuarioId")
    public ResponseEntity<List<PushTokenResponseDTO>> visualizarStatusByUsuario(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated
    ) {
        List<PushToken> pushTokenList = pushTokenService.visualizarStatusByUsuario(userAuthenticated.id());
        return ResponseEntity.ok(pushTokenList.stream().map(pushTokenMapper::toResponse).toList());
    }

    // PATCH /notificacoes/pushToken/{usuarioId}
    @Operation(
        summary = "Atualizar um push token",
        description = "Atualiza o push token de um usuário a partir do ID de usuário e do push token"
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id")
    @PatchMapping("/pushToken/{usuarioId}")
    public ResponseEntity<PushTokenResponseDTO> atualizarStatus(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated,

        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId, 

        @Parameter(description = "Informações do push token")
        @RequestBody PushTokenPatchDTO request
    ) {
        if (!usuarioId.equals(userAuthenticated.id())) throw new AuthExceptions.UsuarioNaoAutorizadoException();
        PushToken tokenAtualizado = pushTokenService.atualizarStatus(userAuthenticated.id(), request.token(), request.ativo());
        return ResponseEntity.ok().body(pushTokenMapper.toResponse(tokenAtualizado));
    }
}