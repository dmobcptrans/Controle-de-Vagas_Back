package com.cptrans.petrocarga.modules.agente.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class AgenteExceptions {
    public static class MatriculaAlreadyExists extends DataIntegrityViolationException {
        public MatriculaAlreadyExists() {
            super("Matrícula já cadastrada.");
        }
    }    

    @ExceptionHandler(MatriculaAlreadyExists.class)
    public ResponseEntity<SystemResponse> handleMatriculaAlreadyExistsException(MatriculaAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}
