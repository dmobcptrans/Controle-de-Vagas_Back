package com.cptrans.petrocarga.modules.motorista.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class MotoristaExceptions {
    public static class CnhAlreadyExistsException extends DataIntegrityViolationException {
        public CnhAlreadyExistsException() {
            super("CNH já cadastrada.");
        }
    }

    public static class MotoristaNotFoundException extends EntityNotFoundException{
        public MotoristaNotFoundException() {
            super("Motorista não encontrado ou inativo.");
        }
    }

    public static class MotoristaJaPossuiEmpresaException extends DataIntegrityViolationException{
        public MotoristaJaPossuiEmpresaException() {
            super("Motorista já está vinculado à outra empresa.");
        }
    }

    public static class MotoristaNaoPossuiEmpresaException extends DataIntegrityViolationException{
        public MotoristaNaoPossuiEmpresaException() {
            super("Motorista não possui empresa vinculada.");
        }
    }

    @ExceptionHandler(CnhAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleCnhVencidaException(CnhAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(MotoristaNotFoundException.class)
    public ResponseEntity<SystemResponse> handleMotoristaNotFoundException(MotoristaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }
}
