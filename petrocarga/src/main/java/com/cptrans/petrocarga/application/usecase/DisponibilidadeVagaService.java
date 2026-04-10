package com.cptrans.petrocarga.application.usecase;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.DisponibilidadeVagaRequestDTO;
import com.cptrans.petrocarga.domain.entities.DisponibilidadeVaga;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;
import com.cptrans.petrocarga.domain.repositories.DisponibilidadeVagaRepository;
import com.cptrans.petrocarga.infrastructure.scheduler.handlers.DisponibilidadeVagaScheduler;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class DisponibilidadeVagaService {

    @Autowired
    private DisponibilidadeVagaRepository disponibilidadeVagaRepository;
    @Autowired
    private VagaService vagaService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private DisponibilidadeVagaScheduler disponibilidadeVagaScheduler;

    public DisponibilidadeVaga save (DisponibilidadeVaga disponibilidadeVaga) {
        return disponibilidadeVagaRepository.save(disponibilidadeVaga); 
    }

    public List<DisponibilidadeVaga> saveAll (List<DisponibilidadeVaga> disponibilidadeVaga) {
        return disponibilidadeVagaRepository.saveAll(disponibilidadeVaga); 
    }

    public List<DisponibilidadeVaga> findAll() {
        return disponibilidadeVagaRepository.findAll();
    }

    public DisponibilidadeVaga findById(UUID id) {
        return disponibilidadeVagaRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("DisponibilidadeVaga não encontrada."));
    }

    public List<DisponibilidadeVaga> findByVagaId(UUID vagaId) {
        return disponibilidadeVagaRepository.findByVagaId(vagaId);
    }

    public List<DisponibilidadeVaga> findByMes(Integer mes, Integer ano) {
        OffsetDateTime inicioMes = DateUtils.toLocalDateInBrazil(OffsetDateTime.of((int)ano, (int)mes, 1, 0, 0, 0, 0, ZoneOffset.of(DateUtils.FUSO_BRASIL.toString()))).atStartOfDay(DateUtils.FUSO_BRASIL).withDayOfMonth(1).toOffsetDateTime();
        Integer ultimoDiaMes = DateUtils.toLocalDateInBrazil(inicioMes).lengthOfMonth();
        OffsetDateTime fimMes = DateUtils.toLocalDateInBrazil(inicioMes).withDayOfMonth(ultimoDiaMes).atTime(23, 59, 59).atZone(DateUtils.FUSO_BRASIL).toOffsetDateTime();
        return disponibilidadeVagaRepository.findByInicioGreaterThanAndFimLessThan(inicioMes, fimMes);
    }

    public DisponibilidadeVaga createDisponibilidadeVaga(DisponibilidadeVaga novaDisponibilidadeVaga, UUID vagaId) {
        UserAuthenticated usuarioLogado = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioService.findById(usuarioLogado.id());
        Vaga vaga = vagaService.findById(vagaId);
        if(!disponibilidadeValida(novaDisponibilidadeVaga, vaga)) throw new IllegalArgumentException("Informações inválidas.");
        novaDisponibilidadeVaga.setVaga(vaga);
        novaDisponibilidadeVaga.setCriadoPor(usuario);

        DisponibilidadeVaga disponibilidadeCriada = disponibilidadeVagaRepository.save(novaDisponibilidadeVaga);
        
        agendarInicioEfim(disponibilidadeCriada);

        return disponibilidadeCriada;
    }

    public List<DisponibilidadeVaga> createMultipleDisponibilidadeVagas(DisponibilidadeVaga novaDisponibilidadeVaga, List<UUID> listaVagaId) {
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuarioLogado = usuarioService.findById(userAuthenticated.id());
        List<Vaga> listaVagas = new ArrayList<>();
        List<DisponibilidadeVaga> disponibilidadesCriadas = new ArrayList<>();

        if(listaVagaId.isEmpty()) throw new IllegalArgumentException("A lista de vagas não pode estar vazia.");
        
        listaVagaId.forEach(id -> {
            Vaga vaga = vagaService.findById(id);
            listaVagas.add(vaga);
        });

        for(Vaga vaga : listaVagas) {
            if(disponibilidadeValida(novaDisponibilidadeVaga, vaga)) {
                DisponibilidadeVaga disponibilidadeVaga = new DisponibilidadeVaga();
                disponibilidadeVaga.setInicio(novaDisponibilidadeVaga.getInicio());
                disponibilidadeVaga.setFim(novaDisponibilidadeVaga.getFim());
                disponibilidadeVaga.setVaga(vaga);
                disponibilidadeVaga.setCriadoPor(usuarioLogado);
                disponibilidadesCriadas.add(disponibilidadeVaga);

            } 
        }
        if(disponibilidadesCriadas.isEmpty()) throw new IllegalArgumentException("Nenhuma disponibilidade foi criada. Verifique os dados informados.");
        List<DisponibilidadeVaga> disponibilidadesSalvas = disponibilidadeVagaRepository.saveAll(disponibilidadesCriadas);
        if(!disponibilidadesSalvas.isEmpty()) {
            disponibilidadesSalvas.forEach(disponibilidade -> {
                agendarInicioEfim(disponibilidade);
            });
        }
        return disponibilidadesSalvas;
    }

    public DisponibilidadeVaga updateDisponibilidadeVaga(UUID disponibilidadeId, DisponibilidadeVagaRequestDTO novaDisponibilidadeVaga) {
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuarioLogado = usuarioService.findById(userAuthenticated.id());
        DisponibilidadeVaga disponibilidadeCadastrada = findById(disponibilidadeId);

        if (novaDisponibilidadeVaga.getVagaId() != null) {
            Vaga vaga = vagaService.findById(novaDisponibilidadeVaga.getVagaId());
            if(!vaga.equals(disponibilidadeCadastrada.getVaga())) disponibilidadeCadastrada.setVaga(vaga);
        } 
        
        if (!usuarioLogado.equals(disponibilidadeCadastrada.getCriadoPor()))disponibilidadeCadastrada.setCriadoPor(usuarioLogado);
        
        if (novaDisponibilidadeVaga.getInicio() != null) disponibilidadeCadastrada.setInicio(novaDisponibilidadeVaga.getInicio());
        
        if (novaDisponibilidadeVaga.getFim() != null) disponibilidadeCadastrada.setFim(novaDisponibilidadeVaga.getFim());
        
        if(!disponibilidadeValida(disponibilidadeCadastrada, disponibilidadeCadastrada.getVaga())) throw new IllegalArgumentException("Informações inválidas.");
        
        DisponibilidadeVaga disponibilidadeAtualizada = disponibilidadeVagaRepository.save(disponibilidadeCadastrada);

        agendarInicioEfim(disponibilidadeAtualizada);

        return disponibilidadeAtualizada;
    }

    public List<DisponibilidadeVaga> updateDisponibilidadeVagaByCodigoPmp(DisponibilidadeVagaRequestDTO novaDisponibilidadeVaga, String codigoPmp) {
        List<DisponibilidadeVaga> disponibilidadeVagas = disponibilidadeVagaRepository.findByVagaEnderecoCodigoPmp(codigoPmp);
        List<DisponibilidadeVaga> disponibilidadesAtualizadas = new ArrayList<>();
        for (DisponibilidadeVaga disponibilidadeVaga : disponibilidadeVagas) {
            updateDisponibilidadeVaga(disponibilidadeVaga.getId(), novaDisponibilidadeVaga);
            disponibilidadesAtualizadas.add(disponibilidadeVaga);
        }
        List<DisponibilidadeVaga> disponibilidadesSalvas = disponibilidadeVagaRepository.saveAll(disponibilidadesAtualizadas);
        if(!disponibilidadesSalvas.isEmpty()) {
            disponibilidadesSalvas.forEach(disponibilidade -> {
                agendarInicioEfim(disponibilidade);
            });
        }
        return disponibilidadesSalvas;
    }

    public List<DisponibilidadeVaga> updateDisponibilidadeVagaByList(DisponibilidadeVagaRequestDTO novaDisponibilidadeVaga, List<UUID> listaIds) {
        List<DisponibilidadeVaga> disponibilidadesAtualizadas = new ArrayList<>();
        for (UUID id : listaIds) {
            DisponibilidadeVaga disponibilidadeVaga = findById(id);
            updateDisponibilidadeVaga(disponibilidadeVaga.getId(), novaDisponibilidadeVaga);
            disponibilidadesAtualizadas.add(disponibilidadeVaga);
        }
        List<DisponibilidadeVaga> disponibilidadesSalvas = disponibilidadeVagaRepository.saveAll(disponibilidadesAtualizadas);
        if(!disponibilidadesSalvas.isEmpty()) {
            disponibilidadesSalvas.forEach(disponibilidade -> {
                agendarInicioEfim(disponibilidade);
            });
        }
        return disponibilidadesSalvas;
    }

    public void deleteById(UUID id) {
        DisponibilidadeVaga disponibilidadeVaga = findById(id);
        disponibilidadeVagaRepository.deleteById(disponibilidadeVaga.getId());
        try {
            disponibilidadeVagaScheduler.cancelarScheduler(disponibilidadeVaga.getId(), StatusVagaEnum.DISPONIVEL);
            disponibilidadeVagaScheduler.cancelarScheduler(disponibilidadeVaga.getId(), StatusVagaEnum.INDISPONIVEL);
        } catch (SchedulerException e) {
                throw new RuntimeException("Erro ao cancelar scheduler de disponibilidade de vaga.", e);
        }
    }

    public void deleteByIdList(List<UUID> listaIds) {
        disponibilidadeVagaRepository.deleteAllById(listaIds);
        listaIds.forEach(id -> {
            try {
                disponibilidadeVagaScheduler.cancelarScheduler(id, StatusVagaEnum.DISPONIVEL);
                disponibilidadeVagaScheduler.cancelarScheduler(id, StatusVagaEnum.INDISPONIVEL);
            } catch (SchedulerException e) {
                    throw new RuntimeException("Erro ao cancelar scheduler de disponibilidade de vaga.", e);
            }
        });
    }

    public void deleteByCodigoPMP(String codigoPMP) {
        List<DisponibilidadeVaga> disponibilidadeVagas = disponibilidadeVagaRepository.findByVagaEnderecoCodigoPmp(codigoPMP);
        disponibilidadeVagaRepository.deleteAll(disponibilidadeVagas);
        disponibilidadeVagas.forEach(disponibilidade -> {
            try {
                disponibilidadeVagaScheduler.cancelarScheduler(disponibilidade.getId(), StatusVagaEnum.DISPONIVEL);
                disponibilidadeVagaScheduler.cancelarScheduler(disponibilidade.getId(), StatusVagaEnum.INDISPONIVEL);
            } catch (SchedulerException e) {
                    throw new RuntimeException("Erro ao cancelar scheduler de disponibilidade de vaga.", e);
            }
        });
    }

    public Boolean disponibilidadeValida(DisponibilidadeVaga novaDisponibilidadeVaga, Vaga vaga) {
        OffsetDateTime agora = OffsetDateTime.now(DateUtils.FUSO_BRASIL);
        if(novaDisponibilidadeVaga.getFim().isBefore(novaDisponibilidadeVaga.getInicio())) {
            throw new IllegalArgumentException("A data de fim deve ser depois da data de inicio.");
        }
        if(novaDisponibilidadeVaga.getFim().equals(novaDisponibilidadeVaga.getInicio())) {
            throw new IllegalArgumentException("A data início e fim devem ser diferentes.");
        }
        if(novaDisponibilidadeVaga.getFim().toInstant().isBefore(agora.toInstant())) {
            throw new IllegalArgumentException("A data de fim deve ser posterior ao horário atual.");
        }
        List<DisponibilidadeVaga> disponibilidadeVagas = findByVagaId(vaga.getId());
        for (DisponibilidadeVaga disponibilidade : disponibilidadeVagas) {
            if(novaDisponibilidadeVaga.getInicio().toInstant().equals(disponibilidade.getInicio().toInstant()) && novaDisponibilidadeVaga.getFim().toInstant().equals(disponibilidade.getFim().toInstant()) && !disponibilidade.getId().equals(novaDisponibilidadeVaga.getId())) {
                System.out.println("inicio: " + novaDisponibilidadeVaga.getInicio());
                System.out.println("fim: " + novaDisponibilidadeVaga.getFim());
                System.out.println("inicio: " + disponibilidade.getInicio());
                System.out.println("fim: " + disponibilidade.getFim());
                throw new IllegalArgumentException("Já existe uma disponibilidade para a vaga de id: " + vaga.getId() + " nesse horario.");
            }
        }
        return true;
    }


    public void agendarInicioEfim(DisponibilidadeVaga disponibilidadeVaga) {
        try {
            disponibilidadeVagaScheduler.cancelarScheduler(disponibilidadeVaga.getId(), StatusVagaEnum.DISPONIVEL);
            disponibilidadeVagaScheduler.cancelarScheduler(disponibilidadeVaga.getId(), StatusVagaEnum.INDISPONIVEL);

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
            throw new RuntimeException(e);
        }
    }


    @Transactional
    public DisponibilidadeVaga alterarDisponibilidade(UUID disponibilidadeId, StatusVagaEnum novoStatus) {
        DisponibilidadeVaga disponibilidadeVaga = findById(disponibilidadeId);
        disponibilidadeVaga.getVaga().setStatus(novoStatus);
        Vaga vagaAtualizada = vagaService.save(disponibilidadeVaga.getVaga());
        disponibilidadeVaga.setVaga(vagaAtualizada);
        return disponibilidadeVagaRepository.save(disponibilidadeVaga);
    }
}
