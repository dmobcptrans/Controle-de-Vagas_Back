package com.cptrans.petrocarga.infrastructure.scheduler.service;

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

import com.cptrans.petrocarga.infrastructure.configs.quartz.QuartzGroups;
import com.cptrans.petrocarga.infrastructure.scheduler.job.notificacao.NotificarCheckInDisponivelJob;
import com.cptrans.petrocarga.infrastructure.scheduler.job.notificacao.NotificarFimProximoJob;

@Service
public class NotificacaoSchedulerService {
    private final Scheduler scheduler;
    private final String CHECKIN_DISPONIVEL = "CHECKIN_DISPONIVEL";
    private final String FIM_PROXIMO = "FIM_PROXIMO";

    public NotificacaoSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Agenda notificacao de check-in disponível.
     * Agenda o job que notifica o usuario sobre a proximidade da reserva.
     * @param usuarioId id do usuario a ser notificado
     * @param reservaId id da reserva em questão
     * @param inicioReserva data e hora de inicio da reserva
     * @throws SchedulerException se houver um erro ao agendar o job
     */
    public void agendarNotificacaoCheckInDisponivel(UUID usuarioId, UUID reservaId, OffsetDateTime inicioReserva) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "envia-notificacao-" + CHECKIN_DISPONIVEL + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
            QuartzGroups.NOTIFICACAO
        );

        if (scheduler.checkExists(jobKey)) {
            return;
        }

        JobDetail job = JobBuilder.newJob(NotificarCheckInDisponivelJob.class)
        .withIdentity("envia-notificacao-" + CHECKIN_DISPONIVEL + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
            QuartzGroups.NOTIFICACAO
        )
        .usingJobData("usuarioId", usuarioId.toString())
        .usingJobData("inicioReserva", inicioReserva.toString())
        .build();

        Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("trigger-envia-notificacao-" + CHECKIN_DISPONIVEL + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
            QuartzGroups.NOTIFICACAO
        ).startAt(Date.from(inicioReserva.minusMinutes(5).toInstant()))
        .build();
        
        scheduler.scheduleJob(job, trigger);

    }

    /**
     * Agenda notificacao de fim de reserva.
     * Agenta o job que notifica o usuario sobre o fim da reserva.
     * @param usuarioId id do usuario a ser notificado
     * @param reservaId id da reserva em questao
     * @param fimReserva data e hora de fim da reserva
     * @throws SchedulerException se houver um erro ao agendar o job
     */
     public void agendarNotificacaoFimProximo(UUID usuarioId, UUID reservaId, OffsetDateTime fimReserva) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "envia-notificacao-" + FIM_PROXIMO + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
            QuartzGroups.NOTIFICACAO
        );

        if (scheduler.checkExists(jobKey)) {
            return;
        }

        JobDetail job = JobBuilder.newJob(NotificarFimProximoJob.class)
        .withIdentity("envia-notificacao-" + FIM_PROXIMO + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
            QuartzGroups.NOTIFICACAO
        )
        .usingJobData("usuarioId", usuarioId.toString())
        .usingJobData("fimReserva", fimReserva.toString())
        .usingJobData("reservaId", reservaId.toString())
        .build();

        Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("trigger-envia-notificacao-" + FIM_PROXIMO + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
            QuartzGroups.NOTIFICACAO
        ).startAt(Date.from(fimReserva.minusMinutes(10).toInstant()))
        .build();
        
        scheduler.scheduleJob(job, trigger);

    }

    /**
     * Cancela o job de notificacao de check-in em disponibilidade.
     * Cancela o job que notifica o usuario sobre a proximidade da reserva.
     * @param usuarioId id do usuário a ser notificado
     * @param reservaId id da reserva em questão
     * @throws SchedulerException se houver um erro ao cancelar o job
     */
    public void cancelarSchedulerCheckIn(UUID usuarioId, UUID reservaId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "envia-notificacao-" + CHECKIN_DISPONIVEL + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
            QuartzGroups.NOTIFICACAO
        );
        if (!scheduler.checkExists(jobKey)) {
            return;
        }
        scheduler.deleteJob(
            JobKey.jobKey(
                "envia-notificacao-" + CHECKIN_DISPONIVEL + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
                QuartzGroups.NOTIFICACAO
            )
        );
    }

    /**
     * Cancela o job de notificacao de fim de reserva.
     * Cancela o job que notifica o usuário sobre o fim da reserva.
     * @param usuarioId id do usuário a ser notificado
     * @param reservaId id da reserva em questão
     * @throws SchedulerException se houver um erro ao cancelar o job
     */
    public void cancelarSchedulerFimProximo(UUID usuarioId, UUID reservaId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "envia-notificacao-" + FIM_PROXIMO + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
            QuartzGroups.NOTIFICACAO
        );
        if (!scheduler.checkExists(jobKey)) {
            return;
        }
        scheduler.deleteJob(
            JobKey.jobKey(
                "envia-notificacao-" + FIM_PROXIMO + "-usuario-" + usuarioId.toString() + "-reserva-" + reservaId.toString(),
                QuartzGroups.NOTIFICACAO
            )
        );
    }
}
