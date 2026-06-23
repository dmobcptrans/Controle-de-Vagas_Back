package com.cptrans.petrocarga.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class DateExceptions {

    public static class FiltroDataInvalidoException extends IllegalArgumentException {
        public FiltroDataInvalidoException() {
            super("Informe OU a data completa OU mês e ano.");
        }
    }

    public static class MesInvalidoException extends IllegalArgumentException {
        public MesInvalidoException() {
            super("Mês deve ser um valor entre 1 e 12.");
        }
    }

    public static class AnoInvalidoException extends IllegalArgumentException {
        public AnoInvalidoException() {
            super("Ano deve ser um valor entre 2026 e 2100.");
        }
    }
    
    @ExceptionHandler(FiltroDataInvalidoException.class)
    public ResponseEntity<SystemResponse> handleFiltroDataInvalida(FiltroDataInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(MesInvalidoException.class)
    public ResponseEntity<SystemResponse> handleMesInvalido(MesInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }
}