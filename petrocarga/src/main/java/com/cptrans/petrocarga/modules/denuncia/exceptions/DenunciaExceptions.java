package com.cptrans.petrocarga.modules.denuncia.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class DenunciaExceptions {
    public static class ReservaStatusInvalidException extends DataIntegrityViolationException {
        public ReservaStatusInvalidException() {
            super("O status da reserva deve ser 'reservada' ou 'ativa'.");
        }
    }   

    public static class DenunciaNotFoundException extends EntityNotFoundException {
        public DenunciaNotFoundException() {
            super("Denuncia não encontrada.");
        }
    }

    public static class DenunciaAlreadyInProgressException extends DataIntegrityViolationException {
        public DenunciaAlreadyInProgressException() {
            super("Denúncia já está em análise ou já foi finalizada.");
        }
    }

    public static class DenunciaStatusInvalidException extends DataIntegrityViolationException {
        public DenunciaStatusInvalidException() {
            super("Status inválido para esta operação.");
        }
    }

    public static class DenunciaAlreadyExistsException extends DataIntegrityViolationException {
        public DenunciaAlreadyExistsException() {
            super("Já existe uma denuncia criada para essa reserva.");
        }
    }

    @ExceptionHandler(ReservaStatusInvalidException.class)
    public ResponseEntity<SystemResponse> handleReservaStatusInvalidException(ReservaStatusInvalidException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(DenunciaNotFoundException.class)
    public ResponseEntity<SystemResponse> handleDenunciaNotFoundException(DenunciaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(DenunciaAlreadyInProgressException.class)
    public ResponseEntity<SystemResponse> handleDenunciaAlreadyInProgressException(DenunciaAlreadyInProgressException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(DenunciaStatusInvalidException.class)
    public ResponseEntity<SystemResponse> handleDenunciaStatusInvalidException(DenunciaStatusInvalidException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(DenunciaAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleDenunciaAlreadyExistsException(DenunciaAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}