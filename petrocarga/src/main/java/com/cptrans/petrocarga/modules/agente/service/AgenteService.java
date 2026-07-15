package com.cptrans.petrocarga.modules.agente.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.agente.dto.mapper.AgenteMapper;
import com.cptrans.petrocarga.modules.agente.dto.request.AgenteFiltrosDTO;
import com.cptrans.petrocarga.modules.agente.dto.request.AgenteRequestDTO;
import com.cptrans.petrocarga.modules.agente.dto.response.AgenteResponseDTO;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.agente.exceptions.AgenteExceptions;
import com.cptrans.petrocarga.modules.agente.exceptions.AgenteExceptions.AgenteNotFoundException;
import com.cptrans.petrocarga.modules.agente.exceptions.AgenteExceptions.MatriculaAlreadyExists;
import com.cptrans.petrocarga.modules.agente.repository.AgenteRepository;
import com.cptrans.petrocarga.modules.agente.specification.AgenteSpecification;
import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgenteService {
    private final AgenteRepository agenteRepository;
    private final UsuarioService usuarioService;
    private final HashService hashService;
    private final CriptoService criptoService;
    private final AgenteMapper agenteMapper;
    private final Sort SORT_ASC = Sort.by("usuario.nome").ascending();
    private final Sort SORT_DESC = Sort.by("usuario.nome").descending();


    /**
     * Retorna um agente com base no seu id.
     * Se o agente não for encontrado, lança uma exceção do tipo AgenteNotFoundException.
     *
     * @param id o id do agente
     * @return o agente com base no seu id
     * @throws AgenteNotFoundException se o agente não for encontrado
     *
    */
    public Agente findByIdAndAtivoTrue(UUID id) {
        return agenteRepository.findByIdAndUsuarioAtivo(id, true)
                .orElseThrow(() -> new AgenteExceptions.AgenteNotFoundException());
    }

    /**
     * Cria um novo agente com base nos dados passados.
     * Só permite que o agente seja criado por um usuário com permissão de ADMIN ou GESTOR.
     * Se a matrícula do agente já estiver cadastrada, lança uma exceção do tipo MatriculaAlreadyExists.
     *
     * @param novoAgente o objeto com os dados do agente
     * @return o objeto criado com base nos dados do agente
     * @throws MatriculaAlreadyExists se a matrícula do agente já estiver cadastrada
     */
    @Transactional
    public Agente createAgente(AgenteRequestDTO request) {
        if (agenteRepository.existsByMatricula(request.getMatricula())) throw new AgenteExceptions.MatriculaAlreadyExists();

        Usuario usuario = usuarioService.createUsuario(new UsuarioRequestDTO(request.getNome(), request.getTelefone(), request.getEmail(), null, false), request.getCpf(), PermissaoEnum.AGENTE);
        
        String cpf = request.getCpf().trim();
        String cpfHash = hashService.hash(cpf);
        String cpfCripto = criptoService.encrypt(cpf);
        String cpfLast5 = UsuarioUtils.gerarLastN(cpf, 5);
        
        Agente novoAgente = new Agente(
            usuario,
            request.getMatricula().trim(),
            cpfHash,
            cpfCripto,
            cpfLast5
        );
        
        return agenteRepository.save(novoAgente);
    }

    /**
     * Atualiza um agente com base nos dados passados.
     * Só permite que o agente seja atualizado por um usuário com permissão de ADMIN ou GESTOR ou pelo seu próprio dono.
     * Se a matrícula do agente já estiver cadastrada para outro agente, lança uma exceção do tipo MatriculaAlreadyExists.
     *
     * @param usuarioId o id do usuário do agente
     * @param novoAgente o objeto com os dados do agente
     * @return o objeto atualizado com base nos dados do agente
     * @throws MatriculaAlreadyExists se a matrícula do agente já estiver cadastrada para outro agente
     */
    @Transactional
    public Agente updateAgenteById(UUID id, UsuarioPATCHRequestDTO novoAgente) {
        Agente agenteCadastrado = findByIdAndAtivoTrue(id);

        if (novoAgente.getMatricula() != null) {
            if (agenteRepository.existsByMatriculaAndIdNot(novoAgente.getMatricula().trim(), id)) throw new AgenteExceptions.MatriculaAlreadyExists();
            agenteCadastrado.setMatricula(novoAgente.getMatricula());
        }
    
        Usuario usuarioAtualizado = usuarioService.patchUpdate(id, PermissaoEnum.AGENTE, novoAgente);
        agenteCadastrado.setUsuario(usuarioAtualizado);

        if (novoAgente.getCpf() != null) {
            String cpf = novoAgente.getCpf().trim();
            String cpfHash = hashService.hash(cpf);
            String cpfCripto = criptoService.encrypt(cpf);
            String cpfLast5 = UsuarioUtils.gerarLastN(cpf, 5);
            agenteCadastrado.setCpfHash(cpfHash);
            agenteCadastrado.setCpfCripto(cpfCripto);
            agenteCadastrado.setCpfLast5(cpfLast5);
            agenteCadastrado.getUsuario().setPersonalDataKeyVersion(criptoService.getActiveKeyVersion());
        }
        return agenteRepository.save(agenteCadastrado);
    }

    /**
     * Desativa um agente com base no seu id de usuário.
     * Só permite que o agente seja Desativado por um usuário com permissão de ADMIN ou GESTOR.
     *
     * @param usuarioId o id do usuário do agente
     * @throws AgenteNotFoundException se o agente não for encontrado
     */
    public void desativarById(UUID usuarioId) {
        usuarioService.desativarById(usuarioId);
    }

    /**
     * Retorna uma lista de agentes com base nos filtros passados.
     * Os filtros são: nome, matricula e ativo.
     * Se nenhum filtro for passado, então retorna uma lista com todos agentes.
     * 
     * @param filtros o objeto com os filtros
     * @param pagina o numero da pagina, por padrão 0
     * @param tamanhoPagina o tamanho da pagina, por padrão 10
     * @param ordem o tipo de ordem, por padrão ASC
     * @return uma lista de agentes com base nos filtros passados ou todos agentes se nenhum filtro for passado.
     * 
     */
    public PageResponseDTO findByFiltros(AgenteFiltrosDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem != OrdemEnum.ASC ? SORT_DESC : SORT_ASC);
        Page<Agente> page = agenteRepository.findAll(AgenteSpecification.filtrar(filtros), pageable);
        if (page == null || page.isEmpty() || page.getContent().isEmpty()) return new PageResponseDTO(page);
        Page<AgenteResponseDTO> pageResponse = page.map(agenteMapper::toResponse);    
        return new PageResponseDTO(pageResponse);
    }

}