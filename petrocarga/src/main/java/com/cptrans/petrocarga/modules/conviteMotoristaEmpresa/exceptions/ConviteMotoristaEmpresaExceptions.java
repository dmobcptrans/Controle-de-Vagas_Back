package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.enums.StatusConviteMotoristaEmpresaEnum;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class ConviteMotoristaEmpresaExceptions {

    public static class ConviteNotFoundException extends EntityNotFoundException {
        public ConviteNotFoundException() {
            super("Convite não encontrado ou expirado.");
        }
    }

    public static class ConviteInvalidoException extends DataIntegrityViolationException {
        public ConviteInvalidoException() {
            super("O convite já foi respondido ou está expirado.");
        }
    }

    public static class RespostaInvalidaExceptions extends DataIntegrityViolationException {
        public RespostaInvalidaExceptions() {
            super("O status da resposta deve ser '" + StatusConviteMotoristaEmpresaEnum.ACEITO + "' ou '" + StatusConviteMotoristaEmpresaEnum.RECUSADO + "''.");
        }
    }

    public static class ConviteAlreadyExistsException extends DataIntegrityViolationException {
        public ConviteAlreadyExistsException() {
            super("Convite já enviado para este motorista, espere o prazo de 7 dias para enviar outro.");
        }
    }

    public static class TokenInvalidoException extends DataIntegrityViolationException {
        public TokenInvalidoException() {
            super("Token não encontrado ou inválido.");
        }
    }

    public static class DadosMotoristaInvalidosException extends DataIntegrityViolationException {
        public DadosMotoristaInvalidosException() {
            super("Dados inválidos para criação do motorista. Verifique e tente novamente.");
        }
    }

    public static class MotoristaJaVinculadoException extends DataIntegrityViolationException {
        public MotoristaJaVinculadoException() {
            super("Este motorista já está vinculado à sua empresa.");
        }
    }

    @ExceptionHandler(ConviteInvalidoException.class)
    public ResponseEntity<SystemResponse> handleConviteInvalidoException(ConviteInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(RespostaInvalidaExceptions.class)
    public ResponseEntity<SystemResponse> handleRespostaInvalidaExceptions(RespostaInvalidaExceptions ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(ConviteNotFoundException.class)
    public ResponseEntity<SystemResponse> handleConviteNotFoundException(ConviteNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(ConviteAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleConviteAlreadyExistsException(ConviteAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<SystemResponse> handleTokenInvalidoException(TokenInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SystemResponse(ex.getMessage(), 401));
    }

    @ExceptionHandler(DadosMotoristaInvalidosException.class)
    public ResponseEntity<SystemResponse> handleDadosMotoristaInvalidosException(DadosMotoristaInvalidosException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(MotoristaJaVinculadoException.class)
    public ResponseEntity<SystemResponse> handleMotoristaJaVinculadoException(MotoristaJaVinculadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}