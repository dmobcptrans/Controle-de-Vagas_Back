package com.cptrans.petrocarga.modules.operacaoVaga.utils;

import java.time.OffsetDateTime;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.cptrans.petrocarga.modules.operacaoVaga.exceptions.OperacaoVagaExceptions;
import com.cptrans.petrocarga.shared.utils.DateUtils;

@Component
public class OperacaoVagaUtils {
    
    public static final void verificarLimiteHorarioOperacaoVaga(Set<OperacaoVaga> listaOperacaoVaga, OffsetDateTime inicioReserva, OffsetDateTime fimReserva) {
        if (listaOperacaoVaga == null || listaOperacaoVaga.isEmpty()) throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
        if (inicioReserva == null || fimReserva == null) throw new OperacaoVagaExceptions.InicioEFimObrigatoriosException();
        String diaInicio = inicioReserva.getDayOfWeek().toString();
        String diaFim = fimReserva.getDayOfWeek().toString();
        if (listaOperacaoVaga.stream().noneMatch(operacao -> operacao.getDiaSemana().getDescricaoIngles().equals(diaInicio) && operacao.getDiaSemana().getDescricaoIngles().equals(diaFim))) {
            throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
        }
        listaOperacaoVaga.forEach(operacao -> {
            if (operacao.getDiaSemana().getDescricaoIngles().equals(DateUtils.toLocalDateInBrazil(inicioReserva).getDayOfWeek().toString()) ){
                if (operacao.getHoraFim().isAfter(inicioReserva.toLocalTime()) && operacao.getHoraInicio().isBefore(fimReserva.toLocalTime())) {
                    return;
                } else {
                    throw new OperacaoVagaExceptions.VagaSemOperacaoNoPeriodoException();
                }
            }
        });
    }
}