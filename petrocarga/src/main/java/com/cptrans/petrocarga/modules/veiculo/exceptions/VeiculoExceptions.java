package com.cptrans.petrocarga.modules.veiculo.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class VeiculoExceptions {
    public static class VeiculoNotFoundException extends RuntimeException {
        public VeiculoNotFoundException() {
            super("Veículo não encontrado ou desativado.");
        }
    }

    public static class VeiculoNaoPertenceEmpresaException extends DataIntegrityViolationException {
        public VeiculoNaoPertenceEmpresaException() {
            super("O veiculo não pertence à uma empresa");
        }
    }

    @ExceptionHandler(VeiculoNotFoundException.class)
    public ResponseEntity<SystemResponse> handleVeiculoNotFoundException(VeiculoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }
}
