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

    public static class ConflitoCpjCnpjProprietarioException extends DataIntegrityViolationException {
        public ConflitoCpjCnpjProprietarioException() {
            super("Veículo deve conter CPF OU CNPJ do proprietário.");
        }
    }

    public static class PlacaInvalidaExceptions extends IllegalArgumentException {
        public PlacaInvalidaExceptions() {
            super("Placa inválida.");
        }
    }

    public static class VeiculoJaCadastradoException extends DataIntegrityViolationException {
        public VeiculoJaCadastradoException() {
            super("Voce ja possui um veiculo cadastrado com essa placa.");
        }
    }

    public static class VeiculoNaoPodeSerDesativadoException extends DataIntegrityViolationException {
        public VeiculoNaoPodeSerDesativadoException() {
            super("Veículo não pode ser desativado pois possui reservas com status 'ativa' ou 'reservada'.");
        }
    }

    public static class VeiculoAlreadyExistsException extends DataIntegrityViolationException {
        public VeiculoAlreadyExistsException() {
            super("Você já possui um veiculo cadastrado com essa placa.");
        }
    }

    @ExceptionHandler(VeiculoNotFoundException.class)
    public ResponseEntity<SystemResponse> handleVeiculoNotFoundException(VeiculoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(VeiculoNaoPertenceEmpresaException.class)
    public ResponseEntity<SystemResponse> handleVeiculoNaoPertenceEmpresaException(VeiculoNaoPertenceEmpresaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(ConflitoCpjCnpjProprietarioException.class)
    public ResponseEntity<SystemResponse> handleConflitoCpjCnpjProprietarioException(ConflitoCpjCnpjProprietarioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(PlacaInvalidaExceptions.class)
    public ResponseEntity<SystemResponse> handlePlacaInvalidaExceptions(PlacaInvalidaExceptions ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(VeiculoJaCadastradoException.class)
    public ResponseEntity<SystemResponse> handleVeiculoJaCadastradoException(VeiculoJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(VeiculoNaoPodeSerDesativadoException.class)
    public ResponseEntity<SystemResponse> handleVeiculoNaoPodeSerDesativadoException(VeiculoNaoPodeSerDesativadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(VeiculoAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleVeiculoAlreadyExistsException(VeiculoAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}
