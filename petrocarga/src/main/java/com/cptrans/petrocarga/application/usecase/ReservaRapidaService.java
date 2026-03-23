package com.cptrans.petrocarga.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.domain.entities.Agente;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.repositories.ReservaRapidaRepository;
import com.cptrans.petrocarga.domain.repositories.ReservaRepository;
import com.cptrans.petrocarga.domain.specification.ReservaRapidaSpecification;
import com.cptrans.petrocarga.infrastructure.scheduler.handlers.ReservaSchedulerService;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.ReservaRapidaUtils;
import com.cptrans.petrocarga.shared.utils.ReservaUtils;

@Service
public class ReservaRapidaService {
    
    @Autowired
    private ReservaRapidaRepository reservaRapidaRepository;
    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private VagaService vagaService;
    @Autowired
    private ReservaRapidaUtils reservaRapidaUtils;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private AgenteService agenteService;
    @Autowired
    private ReservaSchedulerService reservaSchedulerService;
    
    public List<ReservaRapida> findAll(List<StatusReservaEnum> status) {
        if(status == null ) status = new ArrayList<>();
        if(!status.isEmpty()) {
            return reservaRapidaRepository.findByStatusIn(status);
        }
        return reservaRapidaRepository.findAll();
    }

    public Optional<ReservaRapida> findById(UUID id) {
        return reservaRapidaRepository.findById(id);
    }

    public ReservaRapida save (ReservaRapida reservaRapida) {
        return reservaRapidaRepository.save(reservaRapida);
    }

    public List<ReservaRapida> findAllByData(LocalDate data, List<StatusReservaEnum> status) {
        List<ReservaRapida> reservasRapidas = findAll(status);
        if(reservasRapidas.isEmpty()) return reservasRapidas;
        if(data != null) {
            return reservasRapidas.stream()
                .filter(reservaRapida -> DateUtils.toLocalDateInBrazil(reservaRapida.getInicio()).equals(data) || DateUtils.toLocalDateInBrazil(reservaRapida.getFim()).equals(data))
                .toList();
        }
        return reservasRapidas;
        
    }

    public List<ReservaRapida> findByVagaAndStatusIn(Vaga vaga, List<StatusReservaEnum> status) {
        if(status == null ) status = new ArrayList<>();
        if(status.isEmpty()) {
            return reservaRapidaRepository.findByVaga(vaga);
        }
        return reservaRapidaRepository.findByVagaAndStatusIn(vaga, status);
    }

    public List<ReservaRapida> findByVagaAndDataAndStatusIn(Vaga vaga, LocalDate data, List<StatusReservaEnum> status) {
        List<ReservaRapida> reservasRapidas = reservaRapidaRepository.findByVaga(vaga);
        if(status == null ) status = new ArrayList<>();
        if(!status.isEmpty()) {
            reservasRapidas = reservaRapidaRepository.findByVagaAndStatusIn(vaga, status);
        }
        if(data!=null && reservasRapidas!=null && !reservasRapidas.isEmpty()) {
            return reservasRapidas.stream()
                .filter(reservaRapida -> DateUtils.toLocalDateInBrazil(reservaRapida.getInicio()).equals(data) || DateUtils.toLocalDateInBrazil(reservaRapida.getFim()).equals(data))
                .toList();
        } else {
            return reservasRapidas;
        }
    }

    public List<ReservaRapida> findByPlaca(String placa) {  
        return reservaRapidaRepository.findByPlacaIgnoringCaseAndStatus(placa, StatusReservaEnum.ATIVA);
    }

    public List<ReservaRapida> findByAgente(Agente agente) {
        return reservaRapidaRepository.findByAgente(agente);
    }
    
    public List<ReservaRapida> findByAgenteWithFilters(Agente agente, UUID vagaId, String placaVeiculo, LocalDate data, List<StatusReservaEnum> listaStatus) {
       return reservaRapidaRepository.findAll(ReservaRapidaSpecification.filtrar(agente.getUsuario().getId(), vagaId, placaVeiculo, data, listaStatus));
    }

    public ReservaRapida create(ReservaRapida novaReservaRapida) {
        Vaga vagaReserva = vagaService.findById(novaReservaRapida.getVaga().getId());
        List<StatusReservaEnum> listaStatus = new ArrayList<>(List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA));
        List<Reserva>  reservasAtivasNaVaga = reservaRepository.findByVagaAndStatusIn(vagaReserva, listaStatus);
        List<ReservaRapida> reservasRapidasAtivasNaVaga = findByVagaAndStatusIn(vagaReserva, listaStatus);
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuarioLogado = usuarioService.findById(userAuthenticated.id());
        if(usuarioLogado.getPermissao().equals(PermissaoEnum.AGENTE)){
            Agente agenteLogado = agenteService.findByUsuarioId(usuarioLogado.getId());
            novaReservaRapida.setAgente(agenteLogado);
        }

        reservaRapidaUtils.validarQuantidadeReservasPorPlaca(novaReservaRapida.toReservaDTO());
        ReservaUtils.validarTempoMaximoReserva(novaReservaRapida.toReservaDTO(), novaReservaRapida.getVaga());
        reservaRapidaUtils.validarEspacoDisponivelNaVaga(novaReservaRapida, vagaReserva, reservasAtivasNaVaga, reservasRapidasAtivasNaVaga);
        ReservaRapida reservaRapidaCriada = reservaRapidaRepository.save(novaReservaRapida);
        try {
            reservaSchedulerService.agendarFinalizacaoReserva(reservaRapidaCriada.toReservaDTO());
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar finalização da reserva: " + e.getMessage());
        }
        return reservaRapidaCriada;
    }
}
