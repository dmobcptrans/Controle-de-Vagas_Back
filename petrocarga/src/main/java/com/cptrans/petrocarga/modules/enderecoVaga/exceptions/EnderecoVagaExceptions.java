package com.cptrans.petrocarga.modules.enderecoVaga.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class EnderecoVagaExceptions {
    public static class EnderecoVagaNotFoundException extends EntityNotFoundException {
        public EnderecoVagaNotFoundException() {
            super("Endereço não encontrado");
        }
    } 
    
    public static class EnderecoVagaAlreadyExistsException extends DataIntegrityViolationException {
        public EnderecoVagaAlreadyExistsException() {
            super("Logradouro já cadastrado com um bairro diferente, verifique os dados informados e tente novamente.");
        }
    } 
   
}