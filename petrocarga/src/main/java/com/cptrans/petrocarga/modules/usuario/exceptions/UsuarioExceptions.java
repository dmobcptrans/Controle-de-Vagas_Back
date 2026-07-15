package com.cptrans.petrocarga.modules.usuario.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class UsuarioExceptions {
    public static class UsuarioNotFoundException extends RuntimeException {
        public UsuarioNotFoundException() {
            super("Usuário não encontrado ou desativado.");
        }
    }
    
    public static class CpfAlreadyExistsException extends RuntimeException {
        public CpfAlreadyExistsException() {
            super("Cpf já cadastrado.");
        }
    }
    
    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException() {
            super("Email já cadastrado.");
        }
    }

    public static class EmailOrCpfOrCnpjRequiredException extends RuntimeException {
        public EmailOrCpfOrCnpjRequiredException() {
            super("Informe um email ou CPF ou CNPJ.");
        }
    }

    public static class CpfOrCnpjRequiredException extends RuntimeException {
        public CpfOrCnpjRequiredException() {
            super("Informe um CPF ou CNPJ.");
        }
    }
    
    public static class NenhumUsuarioEncontradoByPermissaoException extends EntityNotFoundException {
        public NenhumUsuarioEncontradoByPermissaoException(PermissaoEnum permissao) {
            super("Nenhum usuário encontrado com a permissão " + permissao + ".");
        }
    }

    public static class TermosNotAcceptedException extends DataIntegrityViolationException {
        public TermosNotAcceptedException() {
            super("Os termos de uso devem ser aceitos.");
        }
    }

    public static class CodigoInvalidoOuExpiradoException extends IllegalArgumentException {
        public CodigoInvalidoOuExpiradoException() {
            super("Código inválido ou expirado.");
        }
    }

    public static class PossuiReservaAtivaException extends DataIntegrityViolationException {
        public PossuiReservaAtivaException() {
            super("Usuário possui reserva ativa.");
        }
    }

    public static class CadastroAlreadyCompletedException extends DataIntegrityViolationException {
        public CadastroAlreadyCompletedException() {
            super("Cadastro já está completo.");
        }
    }
    
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<SystemResponse> handleUsuarioNotFoundException(UsuarioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }
    
    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleCpfAlreadyExistsException(CpfAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
    
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<SystemResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(EmailOrCpfOrCnpjRequiredException.class)
    public ResponseEntity<SystemResponse> handleEmailOrCpfOrCnpjRequiredException(EmailOrCpfOrCnpjRequiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(CpfOrCnpjRequiredException.class)
    public ResponseEntity<SystemResponse> handleCpfOrCnpjRequiredException(CpfOrCnpjRequiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(NenhumUsuarioEncontradoByPermissaoException.class)
    public ResponseEntity<SystemResponse> handleNenhumUsuarioEncontradoByPermissaoException(NenhumUsuarioEncontradoByPermissaoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(TermosNotAcceptedException.class)
    public ResponseEntity<SystemResponse> handleTermosNotAccepted(TermosNotAcceptedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(CodigoInvalidoOuExpiradoException.class)
    public ResponseEntity<SystemResponse> handleCodigoInvalidoOuExpiradoException(CodigoInvalidoOuExpiradoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(PossuiReservaAtivaException.class)
    public ResponseEntity<SystemResponse> handlePossuiReservaAtivaException(PossuiReservaAtivaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(CadastroAlreadyCompletedException.class)
    public ResponseEntity<SystemResponse> handleCadastroAlreadyCompletedException(CadastroAlreadyCompletedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}