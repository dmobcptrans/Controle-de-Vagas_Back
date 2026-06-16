package com.cptrans.petrocarga.modules.usuario.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class UsuarioExceptions {
    public static class UsuarioNotFoundException extends RuntimeException {
        public UsuarioNotFoundException() {
            super("Usuário não encontrado ou desativado.");
        }
    }

    public static class CpfAlreadyExistsException extends RuntimeException {
        public CpfAlreadyExistsException() {
            super("Cpf já cadastrado.");
        }
    }

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException() {
            super("Email já cadastrado.");
        }
    }

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<SystemResponse> handleUsuarioNotFoundException(UsuarioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleCpfAlreadyExistsException(CpfAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}
