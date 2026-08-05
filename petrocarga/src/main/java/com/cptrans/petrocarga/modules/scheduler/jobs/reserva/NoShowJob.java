package com.cptrans.petrocarga.modules.scheduler.jobs.reserva;

import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.reserva.service.ReservaService;

import lombok.RequiredArgsConstructor;

@DisallowConcurrentExecution
@Component
@RequiredArgsConstructor
public class NoShowJob implements Job {

    private final ReservaService reservaService;

    /**
     * Executa o job de processar no show.
     * Este job finaliza uma reserva caso o motorista não faça check-in à tempo.
     * @param context contexto do job
     * @throws JobExecutionException se ocorrer algum erro durante a execução do job
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
       UUID reservaId = UUID.fromString(context.getMergedJobDataMap().getString("reservaId"));
       reservaService.processarNoShow(reservaId);
    }
}