package com.cptrans.petrocarga.modules.denuncia.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class DenunciaExceptions {
    public static class ReservaStatusInvalidException extends DataIntegrityViolationException {
        public ReservaStatusInvalidException() {
            super("O status da reserva deve ser 'reservada' ou 'ativa'.");
        }
    }   

    @ExceptionHandler(ReservaStatusInvalidException.class)
    public ResponseEntity<SystemResponse> handleReservaStatusInvalidException(ReservaStatusInvalidException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}