package com.cptrans.petrocarga.modules.operacaoVaga.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.cptrans.petrocarga.modules.operacaoVaga.exceptions.OperacaoVagaExceptions;

@Component
public class OperacaoVagaUtils {
    
    public static void verificarLimiteHorarioOperacaoVaga(
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

        OffsetDateTime cursor = inicioReserva;

        while (cursor.isBefore(fimReserva)) {

            LocalDate dataAtual = cursor.toLocalDate();
            LocalDate proximaData = dataAtual.plusDays(1);

            OffsetDateTime inicioProximoDia = proximaData
                .atStartOfDay(cursor.getOffset()).toOffsetDateTime();

            OffsetDateTime fimTrecho = fimReserva.isBefore(inicioProximoDia)
                ? fimReserva
                : inicioProximoDia;

            LocalTime horaInicio = cursor.toLocalTime();

            /*
            * Se chegou exatamente à meia-noite, o trecho atual
            * representa o restante do dia.
            */
            boolean terminaNoFimDoDia = fimTrecho.equals(inicioProximoDia);

            LocalTime horaFim = terminaNoFimDoDia
                ? LocalTime.of(23, 59)
                : fimTrecho.toLocalTime();

            String diaSemana = dataAtual
                .getDayOfWeek()
                .toString();

            List<OperacaoVaga> operacoesDoDia = listaOperacaoVaga.stream()
                .filter(op ->
                    op.getDiaSemana()
                        .getDescricaoIngles()
                        .equals(diaSemana)
                )
                .sorted(
                    Comparator.comparing(OperacaoVaga::getHoraInicio)
                )
                .toList();

            if (operacoesDoDia.isEmpty()) {
                throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
            }

            /*
            * Verifica se todo o trecho do dia está continuamente coberto
            * pelos intervalos de operação.
            */
            LocalTime cursorHorario = horaInicio;

            for (OperacaoVaga operacao : operacoesDoDia) {

                /*
                * Ignora operações que terminam antes do ponto
                * que precisamos cobrir.
                */
                if (!operacao.getHoraFim().isAfter(cursorHorario)) {
                    continue;
                }

                /*
                * Existe um buraco entre o cursor e o início
                * desta operação.
                */
                if (operacao.getHoraInicio().isAfter(cursorHorario)) {
                    break;
                }

                /*
                * A operação cobre o cursor.
                */
                if (operacao.getHoraFim().isAfter(cursorHorario)) {
                    cursorHorario = operacao.getHoraFim();
                }

                /*
                * Todo o trecho do dia já foi coberto.
                */
                if (!cursorHorario.isBefore(horaFim)) {
                    break;
                }
            }

            /*
            * Se ainda existe horário não coberto, significa que
            * existe um intervalo sem operação dentro da reserva.
            */
            if (cursorHorario.isBefore(horaFim)) {
                throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
            }

            /*
            * Avança para o próximo dia.
            */
            cursor = fimTrecho;
        }
    }
}