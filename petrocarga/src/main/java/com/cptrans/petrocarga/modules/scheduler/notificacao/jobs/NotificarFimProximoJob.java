package com.cptrans.petrocarga.modules.scheduler.notificacao.jobs;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.notificacao.service.NotificacaoService;

import lombok.NoArgsConstructor;

@DisallowConcurrentExecution
@Component
@NoArgsConstructor
public class NotificarFimProximoJob implements Job {

    @Autowired //deve usar o @Autowired + @NoArgsConstructor para que o spring injete a dependência corretamente
    private NotificacaoService notificacaoService;

    /**
     * Executa o job de notificacao de fim de reserva.
     * Este job notifica o usuario sobre o fim da reserva.
     * @param context contexto do job
     * @throws JobExecutionException se ocorrer algum erro durante a execução do job
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        UUID usuarioId = UUID.fromString(
            context.getMergedJobDataMap().getString("usuarioId")
        );

        OffsetDateTime fimReserva = OffsetDateTime.parse(
            context.getMergedJobDataMap().getString("fimReserva")
        );

        notificacaoService.notificarFimProximo(usuarioId, fimReserva);
    }
}