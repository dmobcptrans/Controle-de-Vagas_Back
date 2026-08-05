package com.cptrans.petrocarga.modules.enderecoVaga.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class EnderecoVagaExceptions {
    public static class EnderecoVagaNotFoundException extends EntityNotFoundException {
        public EnderecoVagaNotFoundException() {
            super("Endereço não encontrado");
        }
    }   

    @ExceptionHandler(EnderecoVagaNotFoundException.class)
    public ResponseEntity<SystemResponse> handleEnderecoVagaNotFoundException(EnderecoVagaNotFoundException ex) {
        return ResponseEntity.status(404).body(new SystemResponse(ex.getMessage(), 404));
    }
}