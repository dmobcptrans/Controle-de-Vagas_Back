package com.cptrans.petrocarga.modules.events.conviteMotoristaEmpresa.listener;

import org.quartz.SchedulerException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cptrans.petrocarga.modules.events.conviteMotoristaEmpresa.ConviteCriadoEvent;
import com.cptrans.petrocarga.modules.scheduler.conviteMotoristaEmpresa.handler.ConviteMotoristaEmpresaSchedulerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConviteCriadoListener {

    private final ConviteMotoristaEmpresaSchedulerService schedulerService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConviteCriado(ConviteCriadoEvent event) throws SchedulerException {
        schedulerService.AgendarExclusaoConvite(event.conviteId(), event.validoAte());
    }

}