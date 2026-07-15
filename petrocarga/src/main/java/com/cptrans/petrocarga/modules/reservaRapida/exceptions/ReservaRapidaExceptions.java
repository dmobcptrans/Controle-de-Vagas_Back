package com.cptrans.petrocarga.modules.reservaRapida.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class ReservaRapidaExceptions {
    public static class LimiteDeReservasPorPlacaException extends DataIntegrityViolationException {
        public LimiteDeReservasPorPlacaException(Integer limiteDeReservasPorPlaca) {
            super("Esta placa já atingiu o limite de " + limiteDeReservasPorPlaca + " reservas rápidas. O(a) motorista deverá criar uma reserva normal se cadastrando no sistema.");
        }
    }
    
    @ExceptionHandler(LimiteDeReservasPorPlacaException.class)
    public ResponseEntity<SystemResponse> handleLimiteDeReservasPorPlacaException(LimiteDeReservasPorPlacaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

}
