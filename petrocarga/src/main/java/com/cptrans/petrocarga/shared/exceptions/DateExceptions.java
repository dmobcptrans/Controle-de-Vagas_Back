package com.cptrans.petrocarga.shared.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;
import com.cptrans.petrocarga.shared.utils.DateUtils;

@RestControllerAdvice
public class DateExceptions {

    public static class MesOuAnoNullException extends IllegalArgumentException {
        public MesOuAnoNullException() {
            super("Informe o ano e o mês que deseja filtrar.");
        }
    }

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
            super( "Ano deve ser um valor entre 2026 e " + (DateUtils.agora().getYear() + 100) + ".");
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

    @ExceptionHandler(AnoInvalidoException.class)
    public ResponseEntity<SystemResponse> handleAnoInvalido(AnoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(MesOuAnoNullException.class)
    public ResponseEntity<SystemResponse> handleMesOuAnoNull(MesOuAnoNullException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }
}