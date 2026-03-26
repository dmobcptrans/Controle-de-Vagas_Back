package com.cptrans.petrocarga.interfaces.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.application.dto.AgenteFiltrosDTO;
import com.cptrans.petrocarga.application.dto.AgenteRequestDTO;
import com.cptrans.petrocarga.application.dto.AgenteResponseDTO;
import com.cptrans.petrocarga.application.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.application.usecase.AgenteService;
import com.cptrans.petrocarga.domain.entities.Agente;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;


@RestController
@RequestMapping("/agentes")
public class AgenteController {

    @Autowired
    private AgenteService agenteService;

    @Autowired
    private CriptoUtils criptoUtils;

    /**
     * Retorna uma lista de agentes com base nos filtros passados.
     *
     * Os filtros são: nome, telefone, matricula, ativo e email.
     * Se nenhum filtro for passado, então retorna uma lista com todos agentes.
     *
     * @param nome o nome do agente
     * @param telefone o telefone do agente
     * @param matricula a matricula do agente
     * @param ativo se o agente está ativo
     * @param email o email do agente
     * @return uma lista de agentes com base nos filtros passados ou todos agentes se nenhum filtro for passado.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping
    public ResponseEntity<List<AgenteResponseDTO>> getAllAgentes(@RequestParam(required = false) String nome, @RequestParam(required = false) String telefone, @RequestParam(required = false) String matricula, @RequestParam(required = false) Boolean ativo, @Valid @Email @RequestParam(required = false) String email) {
        if(nome != null || telefone != null || matricula != null || ativo != null || email != null) {
            AgenteFiltrosDTO filtros = new AgenteFiltrosDTO(nome, telefone, matricula, ativo, email);
            List<Agente> agentesFiltrados = agenteService.findByFiltros(filtros);
            List<AgenteResponseDTO> responseFiltrado = agentesFiltrados.stream().map((agente) -> {
                AgenteResponseDTO response = agente.toResponseDTO();
                response.setUsuario(response.getUsuario() == null ? null : criptoUtils.decrypt(response.getUsuario()));
                return response;
            }).toList();
            return ResponseEntity.ok(responseFiltrado);
        }
        List<Agente> agentes = agenteService.findAll();
        List<AgenteResponseDTO> response = agentes.stream().map(agente -> {
            AgenteResponseDTO responseDTO = agente.toResponseDTO();
            responseDTO.setUsuario(responseDTO.getUsuario() == null ? null : criptoUtils.decrypt(responseDTO.getUsuario()));
            return responseDTO;
        }).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna um agente com base no seu id de usuário.
     * Só permite que o agente seja acessado pelo seu próprio dono ou por um usuário com permissão de ADMIN ou GESTOR.
     *
     * @param usuarioId o id do usuário do agente
     * @return o agente com base no seu id de usuário.
     */
    @PreAuthorize(" #usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/{usuarioId}")
    public ResponseEntity<AgenteResponseDTO> getAgenteById(@PathVariable UUID usuarioId) {
        Agente agente = agenteService.findByUsuarioId(usuarioId);
        AgenteResponseDTO response = agente.toResponseDTO();
        response.setUsuario(response.getUsuario() == null ? null : criptoUtils.decrypt(response.getUsuario()));
        return ResponseEntity.ok(response);
    }

    /**
     * Cria um novo agente com base nos dados passados.
     * Só permite que o agente seja criado por um usuário com permissão de ADMIN ou GESTOR.
     * @param agenteRequestDTO o objeto com os dados do agente
     * @return o objeto criado com base nos dados do agente
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<AgenteResponseDTO> createAgente(@RequestBody @Valid AgenteRequestDTO agenteRequestDTO) {
        Agente savedAgente = agenteService.createAgente(agenteRequestDTO.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAgente.toResponseDTO());
    }

    /**
     * Atualiza um agente com base nos dados passados.
     * Só permite que o agente seja atualizado por um usuário com permissão de ADMIN ou GESTOR ou pelo seu próprio dono.
     * @param usuarioId o id do usuário do agente
     * @param agenteRequestDTO o objeto com os dados do agente
     * @return o objeto atualizado com base nos dados do agente
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR') or #usuarioId == authentication.principal.id")
    @PatchMapping("/{usuarioId}")
    public ResponseEntity<AgenteResponseDTO> updateAgente(@PathVariable UUID usuarioId, @RequestBody @Valid UsuarioPATCHRequestDTO agenteRequestDTO) {
        Agente updatedAgente = agenteService.updateAgente(usuarioId, agenteRequestDTO);
        return ResponseEntity.ok(updatedAgente.toResponseDTO());
    }

    /**
     * Deleta um agente com base no seu id de usuário.
     * Só permite que o agente seja deletado por um usuário com permissão de ADMIN ou GESTOR.
     * @param usuarioId o id do usuário do agente
     * @return uma resposta sem conteúdo caso a exclusão seja realizada com sucesso
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> deleteAgente(@PathVariable UUID usuarioId) {
        agenteService.deleteByUsuarioId(usuarioId);
        return ResponseEntity.noContent().build();
    }
}