package com.cptrans.petrocarga.modules.disponibilidadeVaga.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class DisponibilidadeVagaExceptions {
    public static class DisponibilidadeVagaNotFoundException extends EntityNotFoundException {
        public DisponibilidadeVagaNotFoundException() {
            super("DisponibilidadeVaga não encontrada.");
        }
    }

    public static class HorarioInvalidoException extends IllegalArgumentException {
        public HorarioInvalidoException() {
            super("O horário de fim deve ser posterior ao horário de inicio e ao horário atual.");
        }
    }

    public static class DisponibilidadeVagaAlreadyExistsException extends DataIntegrityViolationException {
        public DisponibilidadeVagaAlreadyExistsException() {
            super("Já existe uma disponibilidade cadastrada para esta vaga neste horario.");
        }
    }

    @ExceptionHandler(DisponibilidadeVagaNotFoundException.class)
    public ResponseEntity<SystemResponse> handleDisponibilidadeVagaNotFoundException(DisponibilidadeVagaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(HorarioInvalidoException.class)
    public ResponseEntity<SystemResponse> handleHorarioInvalidoException(HorarioInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(DisponibilidadeVagaAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleDisponibilidadeVagaAlreadyExistsException(DisponibilidadeVagaAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}