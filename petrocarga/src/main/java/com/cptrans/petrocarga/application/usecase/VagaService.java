package com.cptrans.petrocarga.application.usecase;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cptrans.petrocarga.domain.entities.EnderecoVaga;
import com.cptrans.petrocarga.domain.entities.OperacaoVaga;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.DiaSemanaEnum;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;
import com.cptrans.petrocarga.domain.repositories.VagaRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional; 

@Service
public class VagaService {
    @Autowired
    private VagaRepository vagaRepository;
    @Autowired
    private EnderecoVagaService enderecoVagaService;
    @Autowired
    private OperacaoVagaService operacaoVagaService;

    public Vaga save(Vaga vaga) {
        return vagaRepository.save(vaga);
    }

    public List<Vaga> findAll() {
        return vagaRepository.findAll();
    }

    public List<Vaga> findAllByStatus(StatusVagaEnum status) {
        return vagaRepository.findByStatus(status);
    }
    
    public Page<Vaga> findAllPaginadas(Integer numeroPagina, Integer tamanhoPagina, String ordenarPor, StatusVagaEnum status, String logradouro) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by(ordenarPor).ascending());

        Page<Vaga> vagasPage;
        boolean hasLogradouro = StringUtils.hasText(logradouro); 

        if (status != null && hasLogradouro) {
            vagasPage = vagaRepository.findByStatusAndEnderecoLogradouroContainingIgnoreCase(status, logradouro, pageable);
        } else if (status != null) {
            vagasPage = vagaRepository.findByStatus(status, pageable);
        } else if (hasLogradouro) {
            vagasPage = vagaRepository.findByEnderecoLogradouroContainingIgnoreCase(logradouro, pageable);
        } else {
            vagasPage = vagaRepository.findAll(pageable);
        }

        return vagasPage;
    }

    public Vaga findById(UUID id) {
        return vagaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Vaga com ID " + id + " não encontrada."));
    }
    
    public void deleteById(UUID id) {
        Vaga vaga = vagaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Vaga com ID " + id + " não encontrada."));
        
        vagaRepository.deleteById(vaga.getId());
    }

    @Transactional
    public Vaga updateById(UUID id, Vaga novaVaga) {
        Vaga vagaExistente = vagaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Vaga com ID " + id + " não encontrada."));

        if(novaVaga.getEndereco() != null){
            EnderecoVaga novoEndereco = enderecoVagaService.cadastrarEnderecoVaga(novaVaga.getEndereco());
            vagaExistente.setEndereco(novoEndereco);
        } 
        
        if(novaVaga.getArea() != null) vagaExistente.setArea(novaVaga.getArea());
        if(novaVaga.getNumeroEndereco() != null) vagaExistente.setNumeroEndereco(novaVaga.getNumeroEndereco());
        if(novaVaga.getReferenciaEndereco() != null) vagaExistente.setReferenciaEndereco(novaVaga.getReferenciaEndereco());
        if(novaVaga.getTipoVaga() != null) vagaExistente.setTipoVaga(novaVaga.getTipoVaga());
        if(novaVaga.getReferenciaGeoInicio() != null) vagaExistente.setReferenciaGeoInicio(novaVaga.getReferenciaGeoInicio());
        if(novaVaga.getReferenciaGeoFim() != null) vagaExistente.setReferenciaGeoFim(novaVaga.getReferenciaGeoFim());
        if(novaVaga.getComprimento() != null) vagaExistente.setComprimento(novaVaga.getComprimento());
        if(novaVaga.getStatus() != null) vagaExistente.setStatus(novaVaga.getStatus());
        if (novaVaga.getOperacoesVaga() != null) {
            Map<DiaSemanaEnum, OperacaoVaga> mapaExistentes = vagaExistente.getOperacoesVaga()
                .stream()
                .collect(Collectors.toMap(OperacaoVaga::getDiaSemana, o -> o));

            Map<DiaSemanaEnum, OperacaoVaga> mapaNovas = novaVaga.getOperacoesVaga()
                .stream()
                .collect(Collectors.toMap(OperacaoVaga::getDiaSemana, o -> o, (o1, o2) -> o1)); // caso venha duplicado, mantém o primeiro

            for (OperacaoVaga novaOperacao : mapaNovas.values()) {
                OperacaoVaga existente = mapaExistentes.get(novaOperacao.getDiaSemana());
                if (existente != null) {
                    existente.setHoraInicio(novaOperacao.getHoraInicio());
                    existente.setHoraFim(novaOperacao.getHoraFim());
                } else {
                    novaOperacao.setVaga(vagaExistente);
                    vagaExistente.getOperacoesVaga().add(novaOperacao);
                }
            }
            vagaExistente.getOperacoesVaga().removeIf(
                operacao -> !mapaNovas.containsKey(operacao.getDiaSemana())
            );
        }
        return vagaRepository.save(vagaExistente);
    }
    
    @Transactional()
    public Vaga createVaga(Vaga novaVaga){
        if(novaVaga.getComprimento() == null) {
            throw new IllegalArgumentException("O campo 'comprimento' é obrigatório e não pode ser nulo ou vazio.");
        }
        EnderecoVaga enderecoVaga = enderecoVagaService.cadastrarEnderecoVaga(novaVaga.getEndereco());
        novaVaga.setEndereco(enderecoVaga);

        novaVaga.setStatus(StatusVagaEnum.INDISPONIVEL);
        
        Vaga vagaCadastrada= vagaRepository.save(novaVaga);

        if(vagaCadastrada.getOperacoesVaga() == null || vagaCadastrada.getOperacoesVaga().isEmpty()) {
            vagaCadastrada.setOperacoesVaga(operacaoVagaService.setOperacoesVagaDefault(vagaCadastrada));
        }
        return vagaCadastrada;
    }
}