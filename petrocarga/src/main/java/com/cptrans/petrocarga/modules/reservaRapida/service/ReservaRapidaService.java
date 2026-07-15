package com.cptrans.petrocarga.modules.reservaRapida.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.agente.service.AgenteService;
import com.cptrans.petrocarga.modules.auth.utils.AuthUtils;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.service.DisponibilidadeVagaService;
import com.cptrans.petrocarga.modules.operacaoVaga.utils.OperacaoVagaUtils;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.reservaRapida.dto.mapper.ReservaRapidaMapper;
import com.cptrans.petrocarga.modules.reservaRapida.dto.request.ReservaRapidaRequestDTO;
import com.cptrans.petrocarga.modules.reservaRapida.dto.response.ReservaRapidaResponseDTO;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.reservaRapida.repository.ReservaRapidaRepository;
import com.cptrans.petrocarga.modules.reservaRapida.specification.ReservaRapidaSpecification;
import com.cptrans.petrocarga.modules.reservaRapida.utils.ReservaRapidaUtils;
import com.cptrans.petrocarga.modules.scheduler.handlers.ReservaSchedulerService;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.service.VagaService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ReservaRapidaService {
    private final ReservaRapidaRepository reservaRapidaRepository;
    private final VagaService vagaService;
    private final ReservaRapidaUtils reservaRapidaUtils;
    private final ReservaUtils reservaUtils;
    private final AgenteService agenteService;
    private final ReservaSchedulerService reservaSchedulerService;
    private final DisponibilidadeVagaService disponibilidadeVagaService;
    private final ReservaRapidaMapper reservaRapidaMapper;
    
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

    public List<ReservaRapida> findByPlacaAtiva(String placa) {  
        return reservaRapidaRepository.findByPlacaIgnoringCaseAndStatus(placa, StatusReservaEnum.ATIVA);
    }

    public Page<ReservaRapida> findByAgente(UUID agenteId, Integer numeroPagina, Integer tamanhoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by("inicio").descending());

        return reservaRapidaRepository.findByAgenteId(agenteId, pageable);
    }
    
    public PageResponseDTO findByAgenteIdWithFilters(UUID agenteId, UUID vagaId, String placaVeiculo, LocalDate data, List<StatusReservaEnum> listaStatus, Integer mes, Integer ano, Integer numeroPagina, Integer tamanhoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by("inicio").descending());
        Page<ReservaRapida> page = reservaRapidaRepository.findAll(ReservaRapidaSpecification.filtrar(agenteId, vagaId, placaVeiculo, data, mes, ano, listaStatus), pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        Page<ReservaRapidaResponseDTO> pageResponse = page.map(reservaRapidaMapper::toResponse);
        return new PageResponseDTO(pageResponse); 
    }

    public ReservaRapida create(ReservaRapidaRequestDTO request) {
        if (!disponibilidadeVagaService.existsByVagaIdAndInicioAndFim(request.getVagaId(), request.getInicio(), request.getFim())) {
            throw new IllegalArgumentException("A vaga não está disponível para o período selecionado.");
        }

        Vaga vaga = vagaService.findById(request.getVagaId());
        OperacaoVagaUtils.verificarLimiteHorarioOperacaoVaga(vaga.getOperacoesVaga(), request.getInicio(), request.getFim());
        
        UserAuthenticated userAuthenticated = AuthUtils.getUsuarioAutenticado();

        ReservaRapida novaReservaRapida = reservaRapidaMapper.toEntity(request, vaga);
        Agente agenteLogado = agenteService.findByIdAndAtivoTrue(userAuthenticated.id());
        novaReservaRapida.setAgente(agenteLogado);

        if (novaReservaRapida.getCidadeOrigem() == null ){
            novaReservaRapida.setCidadeOrigem("Petrópolis - RJ");
        }
        ReservaDTO novaReservaDTO = reservaRapidaMapper.toReservaDTO(novaReservaRapida, novaReservaRapida.getAgente().getCpfCripto());
        List<ReservaDTO> reservasSoprepostasNaVaga = reservaUtils.getReservasAtivasSobrepostas(request.getInicio(), request.getFim());
        ReservaUtils.validarTempoMaximoReserva(novaReservaRapida.getInicio(), novaReservaRapida.getFim(), novaReservaRapida.getVaga().getArea(), novaReservaRapida.getAgente().getUsuario().getPermissao());
        reservaRapidaUtils.validarQuantidadeReservasPorPlaca(novaReservaDTO, reservasSoprepostasNaVaga);

        if (vaga.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)) {
            Integer novaPosicao = ReservaUtils.encontrarPosicaoDisponivel(vaga.getQuantidade(), vaga.getComprimento(), novaReservaDTO, reservasSoprepostasNaVaga);
            novaReservaRapida.setPosicaoPerpendicular(novaPosicao);
        }
        
        reservaRapidaUtils.validarEspacoDisponivelNaVaga(novaReservaRapida, vaga, reservasSoprepostasNaVaga);
        ReservaRapida reservaRapidaCriada = reservaRapidaRepository.save(novaReservaRapida);
        try {
            reservaSchedulerService.agendarFinalizacaoReserva(reservaRapidaMapper.toReservaDTO(reservaRapidaCriada, reservaRapidaCriada.getAgente().getCpfCripto()));
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar finalização da reserva: " + e.getMessage());
        }
        return reservaRapidaCriada;
    }
}