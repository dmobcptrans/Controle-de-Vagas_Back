package com.cptrans.petrocarga.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.dto.AgenteFiltrosDTO;
import com.cptrans.petrocarga.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.models.Agente;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.repositories.AgenteRepository;
import com.cptrans.petrocarga.specification.AgenteSpecification;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AgenteService {

    @Autowired
    private AgenteRepository agenteRepository;

    @Autowired
    private UsuarioService usuarioService;

    public List<Agente> findAll() {
       return agenteRepository.findAll();
    }

    public Agente findById(UUID id) {
        return agenteRepository.findByIdAndUsuarioAtivo(id, true)
                .orElseThrow(() -> new EntityNotFoundException("Agente nao encontrado"));
    }

    public Agente findByUsuarioId(UUID usuarioId) {
        Usuario usuario = usuarioService.findById(usuarioId);
        return agenteRepository.findByUsuarioAndUsuarioAtivo(usuario, true)
                .orElseThrow(() -> new EntityNotFoundException("Agente não encontrado"));
    }

    @Transactional
    public Agente createAgente(Agente novoAgente) {
        if(agenteRepository.existsByMatricula(novoAgente.getMatricula())) {
            throw new IllegalArgumentException("Matrícula já cadastrada");
        }
        Usuario usuario = usuarioService.createUsuario(novoAgente.getUsuario(), PermissaoEnum.AGENTE,novoAgente.getUsuario().getCpfHash());
        novoAgente.setUsuario(usuario);
        novoAgente.setMatricula(novoAgente.getMatricula());
        return agenteRepository.save(novoAgente);
    }

    @Transactional
    public Agente updateAgente(UUID usuarioId, UsuarioPATCHRequestDTO novoAgente) {
        Agente agenteCadastrado = findByUsuarioId(usuarioId);

        if (novoAgente.getMatricula() != null) {
            Optional<Agente> agenteByMatricula = agenteRepository.findByMatricula(novoAgente.getMatricula());
            if(agenteByMatricula.isPresent() && !agenteByMatricula.get().getId().equals(agenteCadastrado.getId())) {
                throw new IllegalArgumentException("Matrícula já cadastrada");
            }
            agenteCadastrado.setMatricula(novoAgente.getMatricula());
        }
    
        Usuario usuarioAtualizado = usuarioService.patchUpdate(usuarioId, PermissaoEnum.AGENTE, novoAgente);
        agenteCadastrado.setUsuario(usuarioAtualizado);
        
        return agenteRepository.save(agenteCadastrado);
    }

    public void deleteByUsuarioId(UUID usuarioId) {
        usuarioService.deleteById(usuarioId);
    }

    public List<Agente> findByFiltros(AgenteFiltrosDTO filtros) {
        return agenteRepository.findAll(AgenteSpecification.filtrar(filtros));
    }
}