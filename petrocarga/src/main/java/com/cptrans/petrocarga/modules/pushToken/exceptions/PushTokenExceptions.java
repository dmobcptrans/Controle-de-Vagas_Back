package com.cptrans.petrocarga.modules.pushToken.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class PushTokenExceptions {
    public static class PushTokenNotFoundException extends EntityNotFoundException {
        public PushTokenNotFoundException() {
            super("Push Token não encontrado");
        }
    }

    @ExceptionHandler(PushTokenNotFoundException.class)
    public ResponseEntity<SystemResponse> handlePushTokenNotFoundException(PushTokenNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }
}