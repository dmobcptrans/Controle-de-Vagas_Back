package com.cptrans.petrocarga.modules.cripto.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class CriptoExceptions {
    public static class EncryptException extends InternalError {
        public EncryptException() {
            super("Erro ao criptografar os dados.");
        }
    }   

     public static class DecryptException extends InternalError {
        public DecryptException() {
            super("Erro ao descriptografar os dados.");
        }
    } 
    
    public static class HashException extends InternalError {
        public HashException() {
            super("Erro ao gerar hash dos dados.");
        }
    }

    @ExceptionHandler(EncryptException.class)
    public ResponseEntity<SystemResponse> handleEncryptException(EncryptException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SystemResponse(ex.getMessage(), 500));
    }

    @ExceptionHandler(DecryptException.class)
    public ResponseEntity<SystemResponse> handleDecryptException(DecryptException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SystemResponse(ex.getMessage(), 500));
    }

    @ExceptionHandler(HashException.class)
    public ResponseEntity<SystemResponse> handleHashException(HashException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SystemResponse(ex.getMessage(), 500));
    }

}