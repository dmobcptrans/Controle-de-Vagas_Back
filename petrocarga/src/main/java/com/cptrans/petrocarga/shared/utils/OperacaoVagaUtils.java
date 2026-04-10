package com.cptrans.petrocarga.shared.utils;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.domain.entities.Vaga;

@Component
public class OperacaoVagaUtils {
    
    public static final void verificarLimiteHorarioOperacaoVaga(Vaga vaga){
        if(vaga.getOperacoesVaga() == null || vaga.getOperacoesVaga().isEmpty()) return;
        OffsetDateTime agora = OffsetDateTime.now(DateUtils.FUSO_BRASIL);
        vaga.getOperacoesVaga().forEach(operacao -> {
            if(operacao.getDiaSemana().getDescricaoIngles().equals(DateUtils.toLocalDateInBrazil(agora).getDayOfWeek().toString()) ){
                if(operacao.getHoraInicio().isAfter(agora.toLocalTime()) || operacao.getHoraFim().isBefore(agora.toLocalTime()) ){
                    throw new IllegalArgumentException("A vaga não está no horário de operação no momento. Horário de operação hoje: " + operacao.getHoraInicio() + " às " + operacao.getHoraFim() + ".");
                }
            }
        });
    }
}
