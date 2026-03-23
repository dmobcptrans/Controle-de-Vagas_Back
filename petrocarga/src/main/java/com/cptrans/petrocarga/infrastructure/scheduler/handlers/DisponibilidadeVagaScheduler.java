package com.cptrans.petrocarga.infrastructure.scheduler.handlers;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.domain.entities.DisponibilidadeVaga;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;
import com.cptrans.petrocarga.infrastructure.config.quartz.QuartzGroups;
import com.cptrans.petrocarga.infrastructure.scheduler.jobs.disponibilidadeVaga.AlterarDisponibilidadeJob;

@Service
public class DisponibilidadeVagaScheduler {

    private final Scheduler scheduler;

    public DisponibilidadeVagaScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    /**
     * Agenda alteração da disponibilidade de vaga.
     * @param disponibilidade DisponibilidadeVaga a ser alterada
     * @param status StatusVagaEnum do status da disponibilidade
     * @param dataAlteracao Data e hora da alteração
     * @throws SchedulerException Se houver um erro ao agendar o job
     */
    public void AgendarAlteracaoDisponibilidadeVaga(DisponibilidadeVaga disponibilidade, StatusVagaEnum status, OffsetDateTime dataAlteracao) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "alterar-disponibilidade-vaga-" + disponibilidade.getId().toString() + "-status-" + status.name(),
            QuartzGroups.DISPONIBILIDADE_VAGA
        );

        if (scheduler.checkExists(jobKey)) return;

        JobDetail jobDetail = JobBuilder.newJob(AlterarDisponibilidadeJob.class)
            .withIdentity(jobKey)
            .usingJobData("disponibilidadeId", disponibilidade.getId().toString())
            .usingJobData("status", status.name())
            .build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(
                "trigger-alterar-disponibilidade-vaga-" + disponibilidade.getId().toString() + "-status-" + status.name(),
                QuartzGroups.DISPONIBILIDADE_VAGA
            )
            .startAt(Date.from(dataAlteracao.toInstant()))
            .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Cancela o job de alterar a disponibilidade de vaga.
     * @param disponibilidadeId id da disponibilidade a ser cancelada
     * @param status StatusVagaEnum do status da disponibilidade a ser cancelada
     * @throws SchedulerException Se houver um erro ao cancelar o job
     */
    public void cancelarScheduler(UUID disponibilidadeId , StatusVagaEnum status) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "alterar-disponibilidade-vaga-" + disponibilidadeId.toString() + "-status-" + status.name(),
            QuartzGroups.DISPONIBILIDADE_VAGA
        );

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
    }
}
