package com.cptrans.petrocarga.infrastructure.scheduler.handlers;

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

import com.cptrans.petrocarga.application.dto.ReservaDTO;
import com.cptrans.petrocarga.infrastructure.config.quartz.QuartzGroups;
import com.cptrans.petrocarga.infrastructure.scheduler.jobs.reserva.FinalizarReservaJob;
import com.cptrans.petrocarga.infrastructure.scheduler.jobs.reserva.NoShowJob;

@Service
public class ReservaSchedulerService {
    private final Scheduler scheduler;

    public ReservaSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Agenda o job de finalizar reserva.
     * Agenda o job que finaliza uma reserva quando chega o horário de fim da reserva.
     * @param reservaDTO dados da reserva a ser finalizada
     * @throws SchedulerException se houver um erro ao agendar o job
     */
    public void agendarFinalizacaoReserva(ReservaDTO reservaDTO) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "finaliza-reserva-" + reservaDTO.getId(),
            QuartzGroups.RESERVAS
        );

        if (scheduler.checkExists(jobKey)) {
            return;
        }
        
        JobDetail job = JobBuilder.newJob(FinalizarReservaJob.class)
        .withIdentity(
            "finaliza-reserva-" + reservaDTO.getId(),
            QuartzGroups.RESERVAS)
        .usingJobData("reservaId", reservaDTO.getId().toString())
        .build();

        Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(
            "trigger-finaliza-reserva-" + reservaDTO.getId(),
            QuartzGroups.RESERVAS)
        .startAt(Date.from(reservaDTO.getFim().toInstant()))
        .build();

        scheduler.scheduleJob(job, trigger);
    }
   
    /**
     * Agenda o job de finalizar reserva noshow.
     * Agenda o job que finaliza uma reserva quando o motorista não faz check-in à tempo.
     * @param reservaDTO dados da reserva a ser finalizada
     * @throws SchedulerException se houver um erro ao agendar o job
     */
    public void agendarFinalizacaoNoShow(ReservaDTO reservaDTO) throws SchedulerException{
        JobKey jobKey = JobKey.jobKey(
            "finaliza-noshow-reserva" + reservaDTO.getId(),
            QuartzGroups.RESERVAS
        );

        if (scheduler.checkExists(jobKey)) {
            return;
        }

        JobDetail job = JobBuilder.newJob(NoShowJob.class)
        .withIdentity(
            "finaliza-noshow-reserva" + reservaDTO.getId(),
            QuartzGroups.RESERVAS)
        .usingJobData("reservaId", reservaDTO.getId().toString())
        .build();

        Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(
            "trigger-finaliza-noshow-reserva" + reservaDTO.getId(),
            QuartzGroups.RESERVAS)
        .startAt(Date.from(reservaDTO.getInicio().plusMinutes(10).toInstant()))
        .build();

        scheduler.scheduleJob(job, trigger);
    }

    /**
     * Cancela o job de finalizar reserva.
     * Cancela o job que finaliza uma reserva quando chega o horário de fim da reserva.
     * @param reservaId id da reserva a ser cancelada
     * @throws SchedulerException se houver um erro ao cancelar o job
     */
    public void cancelarSchedulerFinalizaReserva(UUID reservaId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("finaliza-reserva-" + reservaId, QuartzGroups.RESERVAS);
        if (!scheduler.checkExists(jobKey)) {
            return;
        }
        scheduler.deleteJob(
            JobKey.jobKey("finaliza-reserva-" + reservaId, QuartzGroups.RESERVAS)
        );
    }

    /**
     * Cancela o job de finalizar reserva noshow.
     * Cancela o job que finaliza uma reserva quando o motorista não faz check-in à tempo.
     * @param reservaId id da reserva a ser cancelada
     * @throws SchedulerException se houver um erro ao cancelar o job
     */
    public void cancelarSchedulerNoShowReserva(UUID reservaId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("finaliza-noshow-reserva" + reservaId, QuartzGroups.RESERVAS);
        if (!scheduler.checkExists(jobKey)) {
            return;
        }
        scheduler.deleteJob(
            JobKey.jobKey("finaliza-noshow-reserva" + reservaId, QuartzGroups.RESERVAS)
        );
    }
}

