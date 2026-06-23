package com.cptrans.petrocarga.modules.reserva.exceptions;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

@RestControllerAdvice
public class ReservaExceptions {
    public static class VagaIndisponivelException extends DataIntegrityViolationException{
        public VagaIndisponivelException() {
            super("A vaga não está disponível para o período selecionado.");
        }
    }

    public static class MotoristaNaoPertenceEmpresaException extends DataIntegrityViolationException{
        public MotoristaNaoPertenceEmpresaException() {
            super("O motorista selecionado não pertence à essa empresa.");
        }
    }

    public static class InicioEFimObrigatoriosException extends IllegalArgumentException{
        public InicioEFimObrigatoriosException() {
            super("O inicio e fim da reserva são obrigatórios.");
        }
    }

    public static class InicioEFimInvalidosException extends IllegalArgumentException{
        public InicioEFimInvalidosException() {
            super("O inicio da reserva deve ser menor que o fim e maior que o horário atual.");
        }
    }

    public static class LimiteDeReservasPorPlacaException extends DataIntegrityViolationException{
        public LimiteDeReservasPorPlacaException(Integer limiteDeReservasPorPlaca) {
            super("Esta placa já atingiu o limite de " + limiteDeReservasPorPlaca + " reservas 'ativas' ou 'reservadas' ao mesmo tempo.");
        }
    }

    public static class PlacaComConflitoDeHorarioException extends DataIntegrityViolationException{
        public PlacaComConflitoDeHorarioException() {
            super("Já existe uma reserva com status 'ativa' ou 'reservada' para esta placa com horário conflitante.");
        }
    }

    public static class VeiculoNaoPertenceException extends DataIntegrityViolationException{
        public VeiculoNaoPertenceException() {
            super("Usuário não pode fazer reserva para um veículo de outro usuário.");
        }
    }

    public static class MotoristaInvalidoException extends DataIntegrityViolationException{
        public MotoristaInvalidoException() {
            super("Usuário não tem permissão para fazer reserva para este motorista.");
        }
    }

    public static class MotoristaComConflitoDeHorarioException extends DataIntegrityViolationException{
        public MotoristaComConflitoDeHorarioException() {
            super("Este motorista já possui uma reserva 'ativa' ou 'reservada' com horário conflitante.");
        }
    }

    public static class VeiculoMaiorQueVagaException extends DataIntegrityViolationException{
        public VeiculoMaiorQueVagaException() {
            super("O veículo selecionado é maior que a vaga.");
        }
    }

    public static class EspacoInsuficienteNoPeriodoException extends DataIntegrityViolationException{
        public EspacoInsuficienteNoPeriodoException() {
            super("Não há espaco suficiente na vaga para a reserva no período solicitado.");
        }
    }

    public static class PosicaoPerpendicularProibidaException extends IllegalArgumentException{
        public PosicaoPerpendicularProibidaException() {
            super("Posição perpendicular só é permitida para vaga do tipo 'perpendicular'.");
        }
    }

    public static class PosicaoPerpendicularInvalidaException extends IllegalArgumentException{
        public PosicaoPerpendicularInvalidaException(Integer quantidadePosicoesVaga) {
            super("Posição perpendicular deve ser um número entre 1 e " + quantidadePosicoesVaga + ".");
        }
    }

    public static class TodasPosicoesOcupadasException extends DataIntegrityViolationException{
        public TodasPosicoesOcupadasException() {
            super("Não há posições disponíveis para esta vaga no período solicitado.");
        }
    }

    @ExceptionHandler(VagaIndisponivelException.class)
    public ResponseEntity<SystemResponse> handleVagaIndisponivelException(VagaIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(MotoristaNaoPertenceEmpresaException.class)
    public ResponseEntity<SystemResponse> handleMotoristaNaoPertenceEmpresaException(MotoristaNaoPertenceEmpresaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(InicioEFimObrigatoriosException.class)
    public ResponseEntity<SystemResponse> handleInicioEFimObrigatoriosException(InicioEFimObrigatoriosException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(InicioEFimInvalidosException.class)
    public ResponseEntity<SystemResponse> handleInicioEFimInvalidosException(InicioEFimInvalidosException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(LimiteDeReservasPorPlacaException.class)
    public ResponseEntity<SystemResponse> handleLimiteDeReservasPorPlacaException(LimiteDeReservasPorPlacaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(PlacaComConflitoDeHorarioException.class)
    public ResponseEntity<SystemResponse> handlePlacaComConflitoDeHorarioException(PlacaComConflitoDeHorarioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(VeiculoNaoPertenceException.class)
    public ResponseEntity<SystemResponse> handleVeiculoNaoPertenceException(VeiculoNaoPertenceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(MotoristaInvalidoException.class)
    public ResponseEntity<SystemResponse> handleMotoristaInvalidoException(MotoristaInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(MotoristaComConflitoDeHorarioException.class)
    public ResponseEntity<SystemResponse> handleMotoristaComConflitoDeHorarioException(MotoristaComConflitoDeHorarioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(VeiculoMaiorQueVagaException.class)
    public ResponseEntity<SystemResponse> handleVeiculoMaiorQueVagaException(VeiculoMaiorQueVagaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(EspacoInsuficienteNoPeriodoException.class)
    public ResponseEntity<SystemResponse> handleEspacoInsuficienteNoPeriodoException(EspacoInsuficienteNoPeriodoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(PosicaoPerpendicularProibidaException.class)
    public ResponseEntity<SystemResponse> handlePosicaoPerpendicularProibidaException(PosicaoPerpendicularProibidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(PosicaoPerpendicularInvalidaException.class)
    public ResponseEntity<SystemResponse> handlePosicaoPerpendicularInvalidaException(PosicaoPerpendicularInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SystemResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(TodasPosicoesOcupadasException.class)
    public ResponseEntity<SystemResponse> handleTodasPosicoesOcupadasException(TodasPosicoesOcupadasException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SystemResponse(ex.getMessage(), 409));
    }
}