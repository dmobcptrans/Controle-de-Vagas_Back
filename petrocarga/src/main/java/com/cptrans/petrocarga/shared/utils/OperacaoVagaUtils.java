package com.cptrans.petrocarga.shared.utils;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.domain.entities.Vaga;

@Component
public class OperacaoVagaUtils {
    
    public static final void verificarLimiteHorarioOperacaoVaga(Vaga vaga, OffsetDateTime inicioReserva, OffsetDateTime fimReserva) {
        if (vaga.getOperacoesVaga() == null || vaga.getOperacoesVaga().isEmpty()) throw new IllegalArgumentException("A vaga não está em operação no momento. Verifique os horários de operação da vaga para mais detalhes.");
        if (inicioReserva == null || fimReserva == null) throw new IllegalArgumentException("As datas de início e fim da reserva são obrigatórias.");
        String diaInicio = inicioReserva.getDayOfWeek().toString();
        String diaFim = fimReserva.getDayOfWeek().toString();
        if (vaga.getOperacoesVaga().stream().noneMatch(operacao -> operacao.getDiaSemana().getDescricaoIngles().equals(diaInicio) && operacao.getDiaSemana().getDescricaoIngles().equals(diaFim))) {
            throw new IllegalArgumentException("A vaga não está em operação no momento. Verifique os horários de operação da vaga para mais detalhes.");
        }
        vaga.getOperacoesVaga().forEach(operacao -> {
            if (operacao.getDiaSemana().getDescricaoIngles().equals(DateUtils.toLocalDateInBrazil(inicioReserva).getDayOfWeek().toString()) ){
                if (operacao.getHoraFim().isAfter(inicioReserva.toLocalTime()) && operacao.getHoraInicio().isBefore(fimReserva.toLocalTime())) {
                    return;
                } else {
                    throw new IllegalArgumentException("A vaga não está no horário de operação no momento. Horário de operação hoje: " + operacao.getHoraInicio() + " às " + operacao.getHoraFim() + ".");
                }
            }
        });
    }
}
