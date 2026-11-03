package com.cptrans.petrocarga.modules.vaga.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class VagaExceptions {
    public static class VagaNotFoundException extends EntityNotFoundException {
        public VagaNotFoundException(){
            super("Vaga não encontrada ou indisponível.");
        }
    }  

    public static class QuantidadePosicoesInvalidaException extends IllegalArgumentException {
        public QuantidadePosicoesInvalidaException(){
            super("O campo 'quantidade' é obrigatório para vagas do tipo PERPENDICULAR e deve ser um número inteiro positivo.");
        }
    }

    public static class ComprimentoInvalidoException extends IllegalArgumentException {
        public ComprimentoInvalidoException(){
            super("O comprimento deve ser um número inteiro positivo entre 1 e 100.");
        }
    }

    public static class TempoPermanenciaInvalidoExcpetion extends IllegalArgumentException {
        public TempoPermanenciaInvalidoExcpetion(Integer tempoMaximo){
            super("O tempo de permanência deve ser um número inteiro positivo menor ou igual a " + tempoMaximo + " horas.");
        }
    }

    public static class BoundingBoxInvalidoException extends IllegalArgumentException {
        public BoundingBoxInvalidoException(){
            super("Parâmetros de bounding box inválidos.");
        }
    }

    public static class VagaAlreadyExistsException extends DataIntegrityViolationException {
        public VagaAlreadyExistsException(){
            super("Já existe uma vaga cadastrada com o mesmo código PMP.");
        }
    }
   
}