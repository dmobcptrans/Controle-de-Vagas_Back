package com.cptrans.petrocarga.infrastructure.scheduler.jobs.disponibilidadeVaga;

import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.application.usecase.DisponibilidadeVagaService;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;

@DisallowConcurrentExecution
@Component
public class AlterarDisponibilidadeJob implements Job {

    @Autowired
    private DisponibilidadeVagaService disponibilidadeVagaService;

/**
 * Executa o job de alterar disponibilidade de vaga.
 * Este job altera o status de uma disponibilidade de vaga.
 * @param context contexto do job
 * @throws JobExecutionException se ocorrer algum erro durante a execução do job
 */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        UUID disponibilidadeId = UUID.fromString(
            context.getMergedJobDataMap().getString("disponibilidadeId")
        );

        StatusVagaEnum novoStatus = StatusVagaEnum.valueOf(
            context.getMergedJobDataMap().getString("status")
        );

        disponibilidadeVagaService.alterarDisponibilidade(disponibilidadeId, novoStatus);
    }
    
}
