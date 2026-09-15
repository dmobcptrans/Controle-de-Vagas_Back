package com.cptrans.petrocarga.modules.operacaoVaga.utils;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.cptrans.petrocarga.modules.operacaoVaga.exceptions.OperacaoVagaExceptions;

@Component
public class OperacaoVagaUtils {
    
    public static final void verificarLimiteHorarioOperacaoVaga(
        Set<OperacaoVaga> listaOperacaoVaga,
        OffsetDateTime inicioReserva,
        OffsetDateTime fimReserva
    ) {
        if (listaOperacaoVaga == null || listaOperacaoVaga.isEmpty()) {
            throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
        }

        if (inicioReserva == null || fimReserva == null) {
            throw new OperacaoVagaExceptions.InicioEFimObrigatoriosException();
        }

        if (!inicioReserva.toInstant().isBefore(fimReserva.toInstant())) {
            throw new OperacaoVagaExceptions.InicioOuFImInvalidoException();
        }

        String diaInicio = inicioReserva.getDayOfWeek().toString();
        String diaFim = fimReserva.getDayOfWeek().toString();

        LocalTime horaInicioReserva = inicioReserva.toLocalTime();
        LocalTime horaFimReserva = fimReserva.toLocalTime();

        // Reserva iniciando e terminando no mesmo dia.
        if (diaInicio.equals(diaFim)) {

            OperacaoVaga operacaoVaga = listaOperacaoVaga.stream()
                .filter(op ->
                    op.getDiaSemana()
                        .getDescricaoIngles()
                        .equals(diaInicio)
                )
                .findFirst()
                .orElse(null);

            if (
                operacaoVaga != null &&
                !operacaoVaga.getHoraInicio().isAfter(horaInicioReserva) &&
                !operacaoVaga.getHoraFim().isBefore(horaFimReserva)
            ) {
                return;
            }

            throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
        }

        // Reserva atravessando para o dia seguinte.
        long diasEntre =
            ChronoUnit.DAYS.between(
                inicioReserva.toLocalDate(),
                fimReserva.toLocalDate()
            );

        if (diasEntre != 1) {
            throw new OperacaoVagaExceptions.InicioOuFImInvalidoException();
        }

        OperacaoVaga operacaoVagaInicio = listaOperacaoVaga.stream()
            .filter(op ->
                op.getDiaSemana()
                    .getDescricaoIngles()
                    .equals(diaInicio)
            )
            .findFirst()
            .orElse(null);

        OperacaoVaga operacaoVagaFim = listaOperacaoVaga.stream()
            .filter(op ->
                op.getDiaSemana()
                    .getDescricaoIngles()
                    .equals(diaFim)
            )
            .findFirst()
            .orElse(null);

        if (operacaoVagaInicio == null || operacaoVagaFim == null) {
            throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
        }

        /*
        * No dia inicial, a reserva precisa começar dentro
        * do horário de operação.
        */
        boolean inicioValido =
            !horaInicioReserva.isBefore(operacaoVagaInicio.getHoraInicio()) &&
            !horaInicioReserva.isAfter(operacaoVagaInicio.getHoraFim());

        /*
        * No dia final, a reserva precisa terminar dentro
        * do horário de operação.
        */
        boolean fimValido =
            !horaFimReserva.isBefore(operacaoVagaFim.getHoraInicio()) &&
            !horaFimReserva.isAfter(operacaoVagaFim.getHoraFim());

        if (!inicioValido || !fimValido) {
            throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
        }
    }
}