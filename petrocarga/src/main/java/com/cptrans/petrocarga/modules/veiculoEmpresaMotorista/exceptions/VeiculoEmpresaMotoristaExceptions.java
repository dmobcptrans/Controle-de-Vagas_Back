package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class VeiculoEmpresaMotoristaExceptions {
    public static class VeiculoEmpresaMotoristaNotFoundException extends EntityNotFoundException {
        public VeiculoEmpresaMotoristaNotFoundException() {
            super("VeiculoEmpresaMotorista não encontrado");
        }
    }

    public static class VeiculoEmpresaMotoristaJaVinculadoException extends DataIntegrityViolationException {
        public VeiculoEmpresaMotoristaJaVinculadoException() {
            super("Motorista já está vinculado ao veículo");
        }
    }

    @ExceptionHandler(VeiculoEmpresaMotoristaNotFoundException.class)
    public ResponseEntity<SystemResponse> handleVeiculoEmpresaMotoristaNotFoundException(VeiculoEmpresaMotoristaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(VeiculoEmpresaMotoristaJaVinculadoException.class)
    public ResponseEntity<SystemResponse> handleVeiculoEmpresaMotoristaJaVinculadoException(VeiculoEmpresaMotoristaJaVinculadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}
