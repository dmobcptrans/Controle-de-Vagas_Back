package com.cptrans.petrocarga.modules.operacaoVaga.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class OperacaoVagaExceptions {
    public static class VagaSemOperacaoNoPeriodoException extends DataIntegrityViolationException {
        public VagaSemOperacaoNoPeriodoException() {
            super("A vaga não está em operação no momento. Verifique os horários de operação da vaga para mais detalhes.");
        }
    }

    public static class InicioEFimObrigatoriosException extends DataIntegrityViolationException {
        public InicioEFimObrigatoriosException() {
            super("As datas de início e fim da reserva são obrigatórias.");
        }
    }

    @ExceptionHandler(VagaSemOperacaoNoPeriodoException.class)
    public ResponseEntity<SystemResponse> handleVagaSemOperacaoNoPeriodoException(VagaSemOperacaoNoPeriodoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}