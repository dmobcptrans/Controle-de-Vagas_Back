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

    public static class MotoristaCadastradadoInativoException extends DataIntegrityViolationException{
        public MotoristaCadastradadoInativoException() {
            super("O Motorista foi encontrado no sistema, porém a conta está inativa.");
        }
    }

    public static class CnhVencidaException extends DataIntegrityViolationException{
        public CnhVencidaException() {
            super("CNH vencida.");
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

    @ExceptionHandler(MotoristaJaPossuiEmpresaException.class)
    public ResponseEntity<SystemResponse> handleMotoristaJaPossuiEmpresaException(MotoristaJaPossuiEmpresaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(MotoristaNaoPossuiEmpresaException.class)
    public ResponseEntity<SystemResponse> handleMotoristaNaoPossuiEmpresaException(MotoristaNaoPossuiEmpresaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(MotoristaCadastradadoInativoException.class)
    public ResponseEntity<SystemResponse> handleMotoristaCadastradadoInativoException(MotoristaCadastradadoInativoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(CnhVencidaException.class)
    public ResponseEntity<SystemResponse> handleCnhVencidaException(CnhVencidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}