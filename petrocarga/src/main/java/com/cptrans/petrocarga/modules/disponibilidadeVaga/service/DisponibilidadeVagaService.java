package com.cptrans.petrocarga.modules.disponibilidadeVaga.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.modules.auth.utils.AuthUtils;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.mapper.DisponibilidadeVagaMapper;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.request.DisponibilidadeVagaRequestDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.request.MultiplasDisponibilidadesVagaRequestDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.dto.response.DisponibilidadeVagaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.entity.DisponibilidadeVaga;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.exceptions.DisponibilidadeVagaExceptions;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.repository.DisponibilidadeVagaRepository;
import com.cptrans.petrocarga.modules.scheduler.disponibilidadeVaga.handler.DisponibilidadeVagaSchedulerService;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.repository.VagaRepository;
import com.cptrans.petrocarga.modules.vaga.service.VagaService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.exceptions.GlobalHandlerExceptions;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DisponibilidadeVagaService {
    private final DisponibilidadeVagaRepository disponibilidadeVagaRepository;
    private final VagaService vagaService;
    private final DisponibilidadeVagaSchedulerService disponibilidadeVagaScheduler;
    private final VagaRepository vagaRepository;
    private final DisponibilidadeVagaMapper disponibilidadeVagaMapper;

    private final Sort SORT_ASC = Sort.by("fim").ascending();
    private final Sort SORT_DESC = Sort.by("fim").descending();

    public PageResponseDTO findAllPaginadoWithOptionalVagaId(UUID vagaId, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem != OrdemEnum.DESC ? SORT_ASC : SORT_DESC);
        
        Page<DisponibilidadeVaga> page;
        if (vagaId != null) page = disponibilidadeVagaRepository.findByVagaId(vagaId, pageable);
        else page = disponibilidadeVagaRepository.findAll(pageable);

        if (page == null || page.isEmpty()) return new PageResponseDTO(page);

        PageResponseDTO response = new PageResponseDTO(page.map(disponibilidadeVagaMapper::toResponse));
        return response;
    }

    public DisponibilidadeVaga findById(UUID id) {
        return disponibilidadeVagaRepository.findById(id).orElseThrow(()-> new DisponibilidadeVagaExceptions.DisponibilidadeVagaNotFoundException());
    }

    public List<DisponibilidadeVaga> findByIdIn(List<UUID> listaIds) {
        return disponibilidadeVagaRepository.findByIdIn(listaIds);
    }

    public List<DisponibilidadeVaga> findByVagaId(UUID vagaId) {
        return disponibilidadeVagaRepository.findByVagaId(vagaId);
    }

    public List<DisponibilidadeVaga> findByOptionalVagaIdAndMesEAno(UUID vagaId, Integer mes, Integer ano) {
        DateUtils.validarFiltroDeMesEAno(mes, ano);
       
        OffsetDateTime inicioMes = DateUtils.getInicioMes(mes, ano);
        OffsetDateTime fimMes = DateUtils.getFimMes(mes, ano);
        
        List<DisponibilidadeVaga> response;

        if (vagaId != null) response = disponibilidadeVagaRepository.findByVagaIdAndFimGreaterThanAndInicioLessThan(vagaId, inicioMes, fimMes);
        else response = disponibilidadeVagaRepository.findByFimGreaterThanAndInicioLessThan(inicioMes, fimMes);
       
        return response;
    }

    public List<DisponibilidadeVagaSimplificadoResponseDTO> getDisponibilidadeVagaSimplificadoByVagaIdMesEAno(UUID vagaId, Integer mes, Integer ano) {
        List<DisponibilidadeVaga> disponibilidadesVaga = findByOptionalVagaIdAndMesEAno(vagaId, mes, ano);
        return disponibilidadeVagaMapper.toResponseSimplificadoList(disponibilidadesVaga);
    }

    public DisponibilidadeVaga createDisponibilidadeVaga(DisponibilidadeVagaRequestDTO request, UUID vagaId) {
        UserAuthenticated usuarioLogado = AuthUtils.getUsuarioAutenticado();
        
        validarHorarioDisponibilidade(request.getInicio(), request.getFim(), vagaId, null);
        
        Vaga vaga = vagaService.findById(vagaId);

        DisponibilidadeVaga novaDisponibilidadeVaga = disponibilidadeVagaMapper.toEntity(request.getInicio(), request.getFim(), vaga, usuarioLogado.id());

        DisponibilidadeVaga disponibilidadeCriada = disponibilidadeVagaRepository.save(novaDisponibilidadeVaga);
        
        agendarInicioEfim(disponibilidadeCriada);

        return disponibilidadeCriada;
    }

    public boolean existsByVagaIdAndInicioAndFim(UUID vagaId, OffsetDateTime inicio, OffsetDateTime fim) {
        return disponibilidadeVagaRepository.existsByVagaIdAndFimGreaterThanAndInicioLessThan(vagaId, inicio, fim);
    }

    public boolean existsByIdNotAndVagaIdAndInicioAndFim(UUID id, UUID vagaId, OffsetDateTime inicio, OffsetDateTime fim) {
        return disponibilidadeVagaRepository.existsByIdNotAndVagaIdAndFimGreaterThanAndInicioLessThan(id, vagaId, inicio, fim);
    }

    public List<DisponibilidadeVaga> createMultipleDisponibilidadeVagas(MultiplasDisponibilidadesVagaRequestDTO request) {
        UserAuthenticated usuarioLogado = AuthUtils.getUsuarioAutenticado();
        List<UUID> listaVagaId = request.getListaVagaId();
        List<DisponibilidadeVaga> disponibilidadesCriadas = new ArrayList<>();
         
        List<Vaga> listaVagas = vagaService.findByIdIn(listaVagaId);

        listaVagas.forEach(vaga -> {
            validarHorarioDisponibilidade(request.getInicio(), request.getFim(), vaga.getId(), null);
            DisponibilidadeVaga disponibilidadeVaga = disponibilidadeVagaMapper.toEntity(request.getInicio(), request.getFim(), vaga, usuarioLogado.id());
            disponibilidadesCriadas.add(disponibilidadeVaga);
        });

        if (disponibilidadesCriadas.isEmpty()) throw new GlobalHandlerExceptions.DadosInvalidosException();
       
        List<DisponibilidadeVaga> disponibilidadesSalvas = disponibilidadeVagaRepository.saveAll(disponibilidadesCriadas);
       
        if (disponibilidadesSalvas != null && !disponibilidadesSalvas.isEmpty()) disponibilidadesSalvas.forEach(d -> agendarInicioEfim(d));
       
        return disponibilidadesSalvas;
    }

    @Transactional
    public DisponibilidadeVaga updateDisponibilidadeVaga(UUID disponibilidadeId, UUID vagaId, OffsetDateTime novoInicio, OffsetDateTime novoFim) {
        UserAuthenticated usuarioLogado = AuthUtils.getUsuarioAutenticado();
        DisponibilidadeVaga disponibilidadeCadastrada = findById(disponibilidadeId);
        OffsetDateTime antigoInicio = disponibilidadeCadastrada.getInicio();
        OffsetDateTime antigoFim = disponibilidadeCadastrada.getFim();

        if (vagaId != null && !vagaId.equals(disponibilidadeCadastrada.getVaga().getId())) {
            Vaga vaga = vagaService.findById(vagaId);
            disponibilidadeCadastrada.setVaga(vaga);
        } 
        
        if (!usuarioLogado.id().equals(disponibilidadeCadastrada.getCriadoPorId())) disponibilidadeCadastrada.setCriadoPorId(usuarioLogado.id());
        
        if (novoInicio != null) disponibilidadeCadastrada.setInicio(DateUtils.fusoHorarioBrasilia(novoInicio));
        
        if (novoFim != null) disponibilidadeCadastrada.setFim(DateUtils.fusoHorarioBrasilia(novoFim));
        
        validarHorarioDisponibilidade(disponibilidadeCadastrada.getInicio(), disponibilidadeCadastrada.getFim(), disponibilidadeCadastrada.getVaga().getId(), disponibilidadeId);
        
        DisponibilidadeVaga disponibilidadeAtualizada = disponibilidadeVagaRepository.save(disponibilidadeCadastrada);

        indisponibilizarVagaSeNecesarioAoAtualizarDisponibilidade(antigoInicio, antigoFim, disponibilidadeAtualizada.getInicio(), disponibilidadeAtualizada.getFim(), disponibilidadeAtualizada.getVaga());

        return disponibilidadeAtualizada;
    }

    public List<DisponibilidadeVaga> updateDisponibilidadeVagaByIdList(DisponibilidadeVagaRequestDTO request, List<UUID> listaIds) {
        List<DisponibilidadeVaga> disponibilidadesAtualizadas = new ArrayList<>();
        
        listaIds.forEach(id -> {
            DisponibilidadeVaga disponibilidadeVaga = findById(id);
            updateDisponibilidadeVaga(disponibilidadeVaga.getId(), request.getVagaId(), request.getInicio(), request.getFim());
            disponibilidadesAtualizadas.add(disponibilidadeVaga);
        });

        if (!disponibilidadesAtualizadas.isEmpty()) disponibilidadesAtualizadas.forEach(d -> agendarInicioEfim(d));

        return disponibilidadesAtualizadas;
    }

    @Transactional
    public void deleteById(UUID id) {
        DisponibilidadeVaga disponibilidadeVaga = findById(id);
        disponibilidadeVagaRepository.deleteById(disponibilidadeVaga.getId());
        
        cancelarScheduler(id);

        indisponibilizarVagaSeNecesarioAoDeletarDisponibilidade(disponibilidadeVaga.getInicio(), disponibilidadeVaga.getFim(), disponibilidadeVaga.getVaga());
    }

    @Transactional
    public void deleteByIdList(List<UUID> listaIds) {
        List<DisponibilidadeVaga> disponibilidadesVagas = findByIdIn(listaIds);
        
        disponibilidadesVagas.forEach(d -> indisponibilizarVagaSeNecesarioAoDeletarDisponibilidade(d.getInicio(), d.getFim(), d.getVaga()));

        disponibilidadeVagaRepository.deleteAllById(listaIds);
        
        listaIds.forEach(id -> cancelarScheduler(id));
    }
    
    @Transactional
    public void alterarStatusVaga(UUID id, StatusVagaEnum status) {
        DisponibilidadeVaga disponibilidadeVaga = findById(id);
        disponibilidadeVaga.getVaga().setStatus(status);
        disponibilidadeVagaRepository.save(disponibilidadeVaga);
    }

    private void validarHorarioDisponibilidade(OffsetDateTime inicio, OffsetDateTime fim, UUID vagaId, UUID id) {
        OffsetDateTime agora = DateUtils.agora();

        if (
            (fim.toInstant().isBefore(inicio.toInstant())) ||
            (fim.toInstant().equals(inicio.toInstant())) ||
            (fim.toInstant().isBefore(agora.toInstant()))
        ) throw new DisponibilidadeVagaExceptions.HorarioInvalidoException();

        if (id != null){
            if (existsByIdNotAndVagaIdAndInicioAndFim(id, vagaId, inicio, fim)) throw new DisponibilidadeVagaExceptions.DisponibilidadeVagaAlreadyExistsException();
        } else {
            if (existsByVagaIdAndInicioAndFim(vagaId, inicio, fim)) throw new DisponibilidadeVagaExceptions.DisponibilidadeVagaAlreadyExistsException();
        }
    }

    private void agendarInicioEfim(DisponibilidadeVaga disponibilidadeVaga) {
        try {
           cancelarScheduler(disponibilidadeVaga.getId());

            disponibilidadeVagaScheduler.AgendarAlteracaoDisponibilidadeVaga(
                disponibilidadeVaga,
                StatusVagaEnum.DISPONIVEL,
                disponibilidadeVaga.getInicio()
            );

            disponibilidadeVagaScheduler.AgendarAlteracaoDisponibilidadeVaga(
                disponibilidadeVaga,
                StatusVagaEnum.INDISPONIVEL,
                disponibilidadeVaga.getFim()
            );
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar scheduler de disponibilidade de vaga.", e);
        }
    }

    private void cancelarScheduler(UUID disponibilidadeId) {
        try {
            disponibilidadeVagaScheduler.cancelarScheduler(disponibilidadeId, StatusVagaEnum.DISPONIVEL);
            disponibilidadeVagaScheduler.cancelarScheduler(disponibilidadeId, StatusVagaEnum.INDISPONIVEL);
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao cancelar scheduler de disponibilidade de vaga." + e);
        }
    }

    @Transactional
    private void indisponibilizarVagaSeNecesarioAoDeletarDisponibilidade(OffsetDateTime inicioDisponibilidade, OffsetDateTime fimDisponibilidade, Vaga vaga) {
        OffsetDateTime agora = DateUtils.agora();
        
        if (fimDisponibilidade.isAfter(agora) && inicioDisponibilidade.isBefore(agora)) {
            vaga.setStatus(StatusVagaEnum.INDISPONIVEL);
            vagaRepository.save(vaga);
        }
    }

    @Transactional
    private void indisponibilizarVagaSeNecesarioAoAtualizarDisponibilidade(
            OffsetDateTime antigoInicioDisponibilidade,
            OffsetDateTime antigoFimDisponibilidade,
            OffsetDateTime novoInicioDisponibilidade, 
            OffsetDateTime novoFimDisponibilidade, 
            Vaga vaga
        ){

        OffsetDateTime agora = DateUtils.agora();
        
        if (
            antigoFimDisponibilidade.isAfter(agora) && 
            antigoInicioDisponibilidade.isBefore(agora) &&
            novoInicioDisponibilidade.isAfter(agora) &&
            novoFimDisponibilidade.isAfter(agora)
        ) {
            vaga.setStatus(StatusVagaEnum.INDISPONIVEL);
            vagaRepository.save(vaga);
        }
    }
}