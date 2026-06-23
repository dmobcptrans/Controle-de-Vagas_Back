package com.cptrans.petrocarga.modules.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class AuthExceptions {
    public static class UsuarioNaoAutenticadoException extends AuthenticationException {
        public UsuarioNaoAutenticadoException() {
            super("Usuário não autenticado.");
        }
    }

    public static class UsuarioNaoAutorizadoException extends AuthorizationDeniedException {
        public UsuarioNaoAutorizadoException() {
            super("Usuário não autorizado.");
        }
    }

    public static class CredenciaisInvalidasException extends AuthenticationException {
        public CredenciaisInvalidasException() {
            super("Credenciais inválidas.");
        }
    }

    @ExceptionHandler(UsuarioNaoAutenticadoException.class)
    public ResponseEntity<SystemResponse> handleUsuarioNaoAutenticadoException(UsuarioNaoAutenticadoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SystemResponse(ex.getMessage(), 401));
    }

    @ExceptionHandler(UsuarioNaoAutorizadoException.class)
    public ResponseEntity<SystemResponse> handleUsuarioNaoAutorizadoException(UsuarioNaoAutorizadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new SystemResponse(ex.getMessage(), 403));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<SystemResponse> handleCredenciaisInvalidasException(CredenciaisInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SystemResponse(ex.getMessage(), 401));
    }
}
