package com.cptrans.petrocarga.modules.motorista.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class MotoristaExceptions {
    public static class CnhAlreadyExistsException extends DataIntegrityViolationException {
        public CnhAlreadyExistsException() {
            super("CNH já cadastrada.");
        }
    }

    @ExceptionHandler(CnhAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleCnhVencidaException(CnhAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}
