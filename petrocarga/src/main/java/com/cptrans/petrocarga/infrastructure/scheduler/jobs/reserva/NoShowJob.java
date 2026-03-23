package com.cptrans.petrocarga.infrastructure.scheduler.jobs.reserva;

import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.cptrans.petrocarga.application.usecase.ReservaService;

public class NoShowJob implements Job {

    @Autowired
    private ReservaService reservaService;

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
