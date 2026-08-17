package com.cptrans.petrocarga.modules.scheduler.handlers;

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

import com.cptrans.petrocarga.config.quartz.QuartzGroups;
import com.cptrans.petrocarga.modules.scheduler.jobs.notificacao.NotificarCheckInDisponivelJob;
import com.cptrans.petrocarga.modules.scheduler.jobs.notificacao.NotificarFimProximoJob;
import com.cptrans.petrocarga.modules.scheduler.jobs.reserva.NovaReservaJob;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacaoSchedulerService {
    private final Scheduler scheduler;
    private final String NOVA_RESERVA = "NOVA_RESERVA";
    private final String CHECKIN_DISPONIVEL = "CHECKIN_DISPONIVEL";
    private final String FIM_PROXIMO = "FIM_PROXIMO";

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

        if (scheduler.checkExists(jobKey)) return;

        JobDetail job = JobBuilder.newJob(NotificarCheckInDisponivelJob.class)
            .withIdentity(jobKey.getName(), jobKey.getGroup())
            .usingJobData("usuarioId", usuarioId.toString())
            .usingJobData("inicioReserva", inicioReserva.toString())
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger-" + jobKey.getName() , jobKey.getGroup())
            .startAt(Date.from(inicioReserva.minusMinutes(5).toInstant()))
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

        if (scheduler.checkExists(jobKey)) return;

        JobDetail job = JobBuilder.newJob(NotificarFimProximoJob.class)
            .withIdentity(jobKey)
            .usingJobData("usuarioId", usuarioId.toString())
            .usingJobData("fimReserva", fimReserva.toString())
            .usingJobData("reservaId", reservaId.toString())
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger-" + jobKey.getName() , jobKey.getGroup())
            .startAt(Date.from(fimReserva.minusMinutes(10).toInstant()))
            .build();
        
        scheduler.scheduleJob(job, trigger);

    }

    public void agendarNotificacaoNovaReserva(UUID empresaId, String empresaNome, UUID reservaId, UUID motoristaId, OffsetDateTime criadoEm) throws SchedulerException {
        System.out.println("agendarNotificacaoNovaReserva - entrei");
        
        JobKey jobKey = JobKey.jobKey(
            "envia-notificacao-" + NOVA_RESERVA + "-empresa-" + empresaId.toString() + "-reserva-" + reservaId.toString() + "-motorista-" + motoristaId.toString(),
            QuartzGroups.NOTIFICACAO
        );

        if (scheduler.checkExists(jobKey)) return;
        System.out.println("criadoEm.toString(): " + criadoEm.toString());
        JobDetail job = JobBuilder.newJob(NovaReservaJob.class)
            .withIdentity(jobKey)
            .usingJobData("empresaId", empresaId.toString())
            .usingJobData("empresaNome", empresaNome.trim())
            .usingJobData("motoristaId", motoristaId.toString())
            .usingJobData("reservaId", reservaId.toString())
            .usingJobData("criadoEm", criadoEm.toString())
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger-" + jobKey.getName() , jobKey.getGroup())
            .startAt(Date.from(criadoEm.toInstant()))
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

        if (!scheduler.checkExists(jobKey)) return;

        scheduler.deleteJob(jobKey);
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

        if (!scheduler.checkExists(jobKey)) return;
        
        scheduler.deleteJob(jobKey);
    }

    public void cancelarSchedulerNovaReserva(UUID empresaId, UUID reservaId, UUID motoristaId) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(
            "envia-notificacao-" + NOVA_RESERVA + "-empresa-" + empresaId.toString() + "-reserva-" + reservaId.toString() + "-motorista-" + motoristaId.toString(),
            QuartzGroups.NOTIFICACAO
        );

        if (!scheduler.checkExists(jobKey)) return;
        
        scheduler.deleteJob(jobKey);
    }
}