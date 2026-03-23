package com.cptrans.petrocarga.application.usecase;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.domain.entities.OperacaoVaga;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.DiaSemanaEnum;
import com.cptrans.petrocarga.domain.repositories.OperacaoVagaRepository;

@Service
public class OperacaoVagaService {

    @Autowired
    private OperacaoVagaRepository operacaoVagaRepository;

    public List<OperacaoVaga> salvarOperacoesVaga(Set<OperacaoVaga> listaOperacaoVaga) {
        return operacaoVagaRepository.saveAll(listaOperacaoVaga);
    }

    public Set<OperacaoVaga> setOperacoesVagaDefault(Vaga vaga) {
        Set<OperacaoVaga> operacoesVaga = new HashSet<>();
        final int HORARIO_DEFAULT_INICIO = 0;
        final int HORARIO_DEFAULT_FIM = 13;
        for(int i = 1; i <= 7; i++) {
            OperacaoVaga operacaoVaga = new OperacaoVaga();
            operacaoVaga.setDiaSemana(DiaSemanaEnum.toEnumByCodigo(i));
            operacaoVaga.setHoraInicio(LocalTime.of(HORARIO_DEFAULT_INICIO, 0));
            operacaoVaga.setHoraFim(LocalTime.of(HORARIO_DEFAULT_FIM, 00));
            operacaoVaga.setVaga(vaga);
            operacoesVaga.add(operacaoVaga);
        }
        return salvarOperacoesVaga(operacoesVaga).stream().collect(Collectors.toSet());
    }
}
