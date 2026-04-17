package com.cptrans.petrocarga.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.ReservaDTO;
import com.cptrans.petrocarga.domain.entities.Agente;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVagaEnum;
import com.cptrans.petrocarga.domain.repositories.ReservaRapidaRepository;
import com.cptrans.petrocarga.domain.repositories.ReservaRepository;
import com.cptrans.petrocarga.domain.specification.ReservaRapidaSpecification;
import com.cptrans.petrocarga.infrastructure.scheduler.handlers.ReservaSchedulerService;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.OperacaoVagaUtils;
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
    @Autowired
    private DisponibilidadeVagaService disponibilidadeVagaService;
    
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

    public List<ReservaRapida> findAllWithFilters(UUID usuarioId, UUID vagaId, String placaVeiculo, LocalDate data, Integer mes, Integer ano, List<StatusReservaEnum> listaStatus) {
        return reservaRapidaRepository.findAll(ReservaRapidaSpecification.filtrar(usuarioId, vagaId, placaVeiculo, data, mes, ano, listaStatus));
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

    public List<ReservaRapida> findByVagaIdAndStatusIn(UUID vagaId, List<StatusReservaEnum> status) {
        if(status == null ) status = new ArrayList<>();
        if(status.isEmpty()) {
            return reservaRapidaRepository.findByVagaId(vagaId);
        }
        return reservaRapidaRepository.findByVagaIdAndStatusIn(vagaId, status);
    }

    public List<ReservaRapida> findByVagaIdAndDataAndStatusIn(UUID vagaId, LocalDate data, List<StatusReservaEnum> status) {
        List<ReservaRapida> reservasRapidas = List.of();
        
        if(status == null ) status = new ArrayList<>();

        if(!status.isEmpty()) {
            reservasRapidas = reservaRapidaRepository.findByVagaIdAndStatusIn(vagaId, status);
        }else{
            reservasRapidas = reservaRapidaRepository.findByVagaId(vagaId);
        }

        if (reservasRapidas.isEmpty()) return reservasRapidas;

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

    public Page<ReservaRapida> findByAgente(UUID agenteId, Integer numeroPagina, Integer tamanhoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by("inicio").descending());

        return reservaRapidaRepository.findByAgenteId(agenteId, pageable);
    }
    
    public Page<ReservaRapida> findByAgenteWithFilters(Agente agente, UUID vagaId, String placaVeiculo, LocalDate data, List<StatusReservaEnum> listaStatus, Integer mes, Integer ano, Integer numeroPagina, Integer tamanhoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by("inicio").descending());
        return reservaRapidaRepository.findAll(ReservaRapidaSpecification.filtrar(agente.getUsuario().getId(), vagaId, placaVeiculo, data, mes, ano, listaStatus), pageable);
    }

    public ReservaRapida create(ReservaRapida novaReservaRapida) {
        if(!disponibilidadeVagaService.existsByVagaIdAndInicioAndFim(novaReservaRapida.getVaga().getId(), novaReservaRapida.getInicio(), novaReservaRapida.getFim())) {
            throw new IllegalArgumentException("A vaga não está disponível para o período selecionado.");
        }
        OperacaoVagaUtils.verificarLimiteHorarioOperacaoVaga(novaReservaRapida.getVaga(), novaReservaRapida.getInicio(), novaReservaRapida.getFim());
        Vaga vagaReserva = vagaService.findById(novaReservaRapida.getVaga().getId());
        List<StatusReservaEnum> listaStatus = new ArrayList<>(List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA));
        List<Reserva>  reservasAtivasNaVaga = reservaRepository.findByVagaIdAndStatusIn(vagaReserva.getId(), listaStatus);
        List<ReservaRapida> reservasRapidasAtivasNaVaga = findByVagaIdAndStatusIn(vagaReserva.getId(), listaStatus);
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuarioLogado = usuarioService.findById(userAuthenticated.id());
        if(usuarioLogado.getPermissao().equals(PermissaoEnum.AGENTE)){
            Agente agenteLogado = agenteService.findByUsuarioId(usuarioLogado.getId());
            novaReservaRapida.setAgente(agenteLogado);
        }
        if (novaReservaRapida.getCidadeOrigem() == null ){
            novaReservaRapida.setCidadeOrigem("Petrópolis - RJ");
        }

        reservaRapidaUtils.validarQuantidadeReservasPorPlaca(novaReservaRapida.toReservaDTO());
        ReservaUtils.validarTempoMaximoReserva(novaReservaRapida.toReservaDTO(), novaReservaRapida.getVaga());
        List<ReservaDTO> reservasTotaisAtivasNaVaga = ReservaUtils.juntarReservas(reservasAtivasNaVaga, reservasRapidasAtivasNaVaga);
        
        if (vagaReserva.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR) && novaReservaRapida.getPosicaoPerpendicular() == null) {
            Integer novaPosicao = ReservaUtils.encontrarPosicaoDisponivel(vagaReserva.getQuantidade(), vagaReserva.getComprimento(), novaReservaRapida.toReservaDTO(), reservasTotaisAtivasNaVaga);
            novaReservaRapida.setPosicaoPerpendicular(novaPosicao);
        }
        
        reservaRapidaUtils.validarEspacoDisponivelNaVaga(novaReservaRapida, vagaReserva, reservasTotaisAtivasNaVaga);
        ReservaRapida reservaRapidaCriada = reservaRapidaRepository.save(novaReservaRapida);
        try {
            reservaSchedulerService.agendarFinalizacaoReserva(reservaRapidaCriada.toReservaDTO());
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar finalização da reserva: " + e.getMessage());
        }
        return reservaRapidaCriada;
    }
}