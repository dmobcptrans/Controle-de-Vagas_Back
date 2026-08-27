package com.cptrans.petrocarga.modules.scheduler.conviteMotoristaEmpresa.handler;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.config.quartz.QuartzGroups;
import com.cptrans.petrocarga.modules.scheduler.conviteMotoristaEmpresa.jobs.ExcluirConviteExpiradoJob;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConviteMotoristaEmpresaSchedulerService {

    private final Scheduler scheduler;
    
    public void AgendarExclusaoConvite(UUID conviteId, OffsetDateTime validoAte) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "excluir-convite-motorista-empresa-" + conviteId.toString(),
            QuartzGroups.CONVITE_MOTORISTA_EMPRESA
        );

        if (scheduler.checkExists(jobKey)) return;

        JobDetail jobDetail = JobBuilder.newJob(ExcluirConviteExpiradoJob.class)
            .withIdentity(jobKey)
            .usingJobData("conviteId", conviteId.toString())
            .build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(
                "trigger-" + jobKey.getName(), jobKey.getGroup())
            .startAt(validoAte.plusMinutes(1).toInstant())
            .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void cancelarScheduler(UUID conviteId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "excluir-convite-motorista-empresa-" + conviteId.toString(),
            QuartzGroups.CONVITE_MOTORISTA_EMPRESA
        );

        if (scheduler.checkExists(jobKey)) scheduler.deleteJob(jobKey);
    }
}