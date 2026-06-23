package com.cptrans.petrocarga.modules.vaga.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class VagaExceptions {
    public static class VagaNotFoundException extends EntityNotFoundException {
        public VagaNotFoundException(){
            super("Vaga não encontrada ou indisponível.");
        }
    }  

    public static class QuantidadeObrigatoriaException extends IllegalArgumentException {
        public QuantidadeObrigatoriaException(){
            super("O campo 'quantidade' é obrigatório para vagas do tipo PERPENDICULAR e deve ser um número inteiro positivo.");
        }
    }

    public static class ComprimentoInvalidoException extends IllegalArgumentException {
        public ComprimentoInvalidoException(){
            super("O comprimento deve ser um número inteiro positivo entre 1 e 100.");
        }
    }

    public static class QuantidadePosicoesInvalidaException extends IllegalArgumentException {
        public QuantidadePosicoesInvalidaException(){
            super("Quantidade de posições deve ser um número inteiro positivo.");
        }
    }

    public static class TempoPermanenciaInvalidoExcpetion extends IllegalArgumentException {
        public TempoPermanenciaInvalidoExcpetion(Integer tempoMaximo){
            super("O tempo de permanência deve ser um número inteiro positivo menor ou igual a " + tempoMaximo + " horas.");
        }
    }

    @ExceptionHandler(VagaNotFoundException.class)
    public ResponseEntity<SystemResponse> handleVagaNotFoundException(VagaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SystemResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(QuantidadeObrigatoriaException.class)
    public ResponseEntity<SystemResponse> handleQuantidadeObrigatoriaException(QuantidadeObrigatoriaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(ComprimentoInvalidoException.class)
    public ResponseEntity<SystemResponse> handleComprimentoInvalidoException(ComprimentoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(QuantidadePosicoesInvalidaException.class)
    public ResponseEntity<SystemResponse> handleQuantidadePosicoesInvalidaException(QuantidadePosicoesInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(TempoPermanenciaInvalidoExcpetion.class)
    public ResponseEntity<SystemResponse> handleTempoPermanenciaInvalidoExcpetion(TempoPermanenciaInvalidoExcpetion ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }
}
