package com.cptrans.petrocarga.modules.empresa.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class EmpresaExceptions {
    public static class CnpjAlreadyExistsException extends DataIntegrityViolationException {
        public CnpjAlreadyExistsException() {
            super("Já existe uma empresa cadastrada com esse CNPJ.");
        }
    }

    public static class EmpresaNotFoundException extends EntityNotFoundException{
        public EmpresaNotFoundException() {
            super("Empresa não encontrada ou desativada.");
        }
    }

    public static class EmpresaPossuiReservaAtivaException extends DataIntegrityViolationException{
        public EmpresaPossuiReservaAtivaException() {
            super("Empresa possui reserva ativa.");
        }
    }

    @ExceptionHandler(CnpjAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleCnpjAlreadyExistsException(CnpjAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(EmpresaNotFoundException.class)
    public ResponseEntity<SystemResponse> handleEmpresaNotFoundException(EmpresaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(EmpresaPossuiReservaAtivaException.class)
    public ResponseEntity<SystemResponse> handleEmpresaPossuiReservaAtivaException(EmpresaPossuiReservaAtivaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}
