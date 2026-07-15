package com.cptrans.petrocarga.modules.gestor.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class GestorExceptions {
    public static class GestorNotFoundException extends RuntimeException {
        public GestorNotFoundException() {
            super("Gestor não encontrado ou desativado.");
        }
    }

    @ExceptionHandler(GestorNotFoundException.class)
    public ResponseEntity<SystemResponse> handleGestorNotFoundException(GestorNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }
}