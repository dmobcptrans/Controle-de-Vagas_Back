package com.cptrans.petrocarga.application.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.AgenteFiltrosDTO;
import com.cptrans.petrocarga.application.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.domain.entities.Agente;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.repositories.AgenteRepository;
import com.cptrans.petrocarga.domain.specification.AgenteSpecification;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AgenteService {

    @Autowired
    private AgenteRepository agenteRepository;

    @Autowired
    private UsuarioService usuarioService;


/**
 * Retorna uma lista de todos os agentes.
 *
 * @return uma lista de todos os agentes
 */
    public List<Agente> findAll() {
       return agenteRepository.findAll();
    }

    /**
     * Retorna um agente com base no seu id.
     * Se o agente nao for encontrado, lança uma exceção do tipo EntityNotFoundException.
     *
     * @param id o id do agente
     * @return o agente com base no seu id
     * @throws EntityNotFoundException se o agente nao for encontrado
     *
    */
    public Agente findById(UUID id) {
        return agenteRepository.findByIdAndUsuarioAtivo(id, true)
                .orElseThrow(() -> new EntityNotFoundException("Agente nao encontrado"));
    }

    /**
     * Retorna um agente com base no seu id de usuário.
     * Se o agente não for encontrado, lança uma exceção do tipo EntityNotFoundException.
     *
     * @param usuarioId o id do usuário do agente
     * @return o agente com base no seu id de usuário
     * @throws EntityNotFoundException se o agente não for encontrado
     */
    public Agente findByUsuarioId(UUID usuarioId) {
        Usuario usuario = usuarioService.findById(usuarioId);
        return agenteRepository.findByUsuarioAndUsuarioAtivo(usuario, true)
                .orElseThrow(() -> new EntityNotFoundException("Agente não encontrado"));
    }

    /**
     * Cria um novo agente com base nos dados passados.
     * Só permite que o agente seja criado por um usuário com permissão de ADMIN ou GESTOR.
     * Se a matrícula do agente já estiver cadastrada, lança uma exceção do tipo IllegalArgumentException.
     *
     * @param novoAgente o objeto com os dados do agente
     * @return o objeto criado com base nos dados do agente
     * @throws IllegalArgumentException se a matrícula do agente já estiver cadastrada
     */
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

    /**
     * Atualiza um agente com base nos dados passados.
     * Só permite que o agente seja atualizado por um usuário com permissão de ADMIN ou GESTOR ou pelo seu próprio dono.
     * Se a matrícula do agente já estiver cadastrada para outro agente, lança uma exceção do tipo IllegalArgumentException.
     *
     * @param usuarioId o id do usuário do agente
     * @param novoAgente o objeto com os dados do agente
     * @return o objeto atualizado com base nos dados do agente
     * @throws IllegalArgumentException se a matrícula do agente já estiver cadastrada para outro agente
     */
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

    /**
     * Deleta um agente com base no seu id de usuário.
     * Só permite que o agente seja deletado por um usuário com permissão de ADMIN ou GESTOR.
     *
     * @param usuarioId o id do usuário do agente
     * @throws EntityNotFoundException se o agente não for encontrado
     */
    public void deleteByUsuarioId(UUID usuarioId) {
        usuarioService.deleteById(usuarioId);
    }

    /**
     * Retorna uma lista de agentes com base nos filtros passados.
     * Os filtros são: nome, telefone, matricula, ativo e email.
     * Se nenhum filtro for passado, então retorna uma lista com todos agentes.
     *
     * @param filtros o objeto com os filtros
     * @return uma lista de agentes com base nos filtros passados ou todos agentes se nenhum filtro for passado.
     * 
     */
    public List<Agente> findByFiltros(AgenteFiltrosDTO filtros) {
        return agenteRepository.findAll(AgenteSpecification.filtrar(filtros));
    }
}