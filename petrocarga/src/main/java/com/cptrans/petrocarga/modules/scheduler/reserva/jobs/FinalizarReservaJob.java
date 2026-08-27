package com.cptrans.petrocarga.modules.scheduler.reserva.jobs;


import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.reserva.service.ReservaService;

import lombok.NoArgsConstructor;

@DisallowConcurrentExecution
@Component
@NoArgsConstructor
public class FinalizarReservaJob implements Job {

    @Autowired //deve usar o @Autowired + @NoArgsConstructor para que o spring injete a dependência corretamente
    private ReservaService reservaService;

    /**
     * Executa o job de finalizar reserva.
     * Este job finaliza uma reserva quando chega o horário de fim da reserva.
     * @param context contexto do job
     * @throws JobExecutionException se ocorrer algum erro durante a execução do job
     */
    @Override
    public void execute(JobExecutionContext context) {
        UUID reservaId = UUID.fromString(
            context.getMergedJobDataMap().getString("reservaId")
        );

        reservaService.finalizarReserva(reservaId);
    }
}