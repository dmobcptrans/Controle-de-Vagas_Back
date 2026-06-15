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
            super("Empresa não encontrada.");
        }
    }

    @ExceptionHandler(CnpjAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleCnpjAlreadyExistsException(CnpjAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}
