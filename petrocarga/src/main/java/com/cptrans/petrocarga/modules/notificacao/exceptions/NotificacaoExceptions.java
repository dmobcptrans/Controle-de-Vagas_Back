package com.cptrans.petrocarga.modules.notificacao.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class NotificacaoExceptions {
    public static class UsuarioNaoPodeEnviarNotificacaoException extends BadCredentialsException {
        public UsuarioNaoPodeEnviarNotificacaoException(PermissaoEnum permissao) {
            super("Usuário com permissão " + permissao + " não pode enviar notificações.");
        }
    }

    public static class NotificacaoNaoEncontradaException extends RuntimeException {
        public NotificacaoNaoEncontradaException() {
            super("Notificação não encontrada.");
        }
    }

    public static class NenhumaNotificacaoEncontradaException extends EntityNotFoundException {
        public NenhumaNotificacaoEncontradaException() {
            super("Nenhuma notificação encontrada.");
        }
    }

    @ExceptionHandler(UsuarioNaoPodeEnviarNotificacaoException.class)
    public ResponseEntity<SystemResponse>handleUsuarioNaoPodeEnviarNotificacaoException(UsuarioNaoPodeEnviarNotificacaoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new SystemResponse(ex.getMessage(), 403));
    }

    @ExceptionHandler(NotificacaoNaoEncontradaException.class)
    public ResponseEntity<SystemResponse>handleNotificacaoNaoEncontradaException(NotificacaoNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(NenhumaNotificacaoEncontradaException.class)
    public ResponseEntity<SystemResponse>handleNenhumaNotificacaoEncontradaException(NenhumaNotificacaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }
}