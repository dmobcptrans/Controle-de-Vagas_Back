package com.cptrans.petrocarga.modules.scheduler.conviteMotoristaEmpresa.jobs;

import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.service.ConviteMotoristaEmpresaService;

import lombok.NoArgsConstructor;

@DisallowConcurrentExecution
@Component
@NoArgsConstructor
public class ExcluirConviteExpiradoJob implements Job{
    @Autowired //deve usar o @Autowired + @NoArgsConstructor para que o spring injete a dependência corretamente
    private ConviteMotoristaEmpresaService service;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        UUID conviteId = UUID.fromString(
            context.getMergedJobDataMap().getString("conviteId")
        );

        service.excluirConviteExpirado(conviteId);
    }
}
