package com.cptrans.petrocarga.interfaces.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.cptrans.petrocarga.application.dto.MotoristaFiltrosDTO;
import com.cptrans.petrocarga.application.dto.MotoristaRequestDTO;
import com.cptrans.petrocarga.application.dto.MotoristaResponseDTO;
import com.cptrans.petrocarga.application.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.application.usecase.MotoristaService;
import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/motoristas")
public class MotoristaController {

    @Autowired
    private MotoristaService motoristaService;


    /**
     * Retorna uma lista de motoristas com base nos filtros passados.
     *
     * Os filtros são: nome, telefone, cnh, ativo.
     * Se nenhum filtro for passado, então retorna uma lista com todos motoristas.
     *
     * @param nome o nome do motorista
     * @param telefone o telefone do motorista
     * @param cnh a cnh do motorista
     * @param ativo se o motorista está ativo
     * @return uma lista de motoristas com base nos filtros passados ou todos motoristas se nenhum filtro for passado.
     */
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping
    public ResponseEntity<List<MotoristaResponseDTO>> getAllMotoristas(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String telefone,
        @RequestParam(required = false) String cnh,
        @RequestParam(required = false) Boolean ativo
    ) {
        MotoristaFiltrosDTO filtros = new MotoristaFiltrosDTO(nome, telefone, cnh, ativo);
        if(filtros.nome() != null || filtros.telefone() != null || filtros.cnh() != null || filtros.ativo() != null) {
            List<MotoristaResponseDTO> motoristasFiltrados = motoristaService.findAllWithFiltros(filtros).stream()
                    .map(motorista -> {
                        MotoristaResponseDTO response = CriptoUtils.decrypt(motorista.toResponseDTO(), motorista.getUsuario().getPersonalDataKeyVersion());
                        return response;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(motoristasFiltrados);
        }
        List<MotoristaResponseDTO> motoristas = motoristaService.findAll().stream()
                .map(motorista -> {
                    MotoristaResponseDTO response = CriptoUtils.decrypt(motorista.toResponseDTO(), motorista.getUsuario().getPersonalDataKeyVersion());
                    return response;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(motoristas);
    }

    /**
     * Retorna um motorista com base no seu id de usuario.
     * Só permite que o motorista seja acessado pelo seu próprio dono ou por um usuário com permissão de ADMIN.
     * 
     * @param usuarioId o id do usuário do motorista
     * @param ativo se o motorista está ativo
     * @return o motorista com base no seu id de usuario
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{usuarioId}")
    public ResponseEntity<MotoristaResponseDTO> getMotoristaById(@PathVariable UUID usuarioId, @RequestParam(required = false) Boolean ativo) {
        Motorista motorista = motoristaService.findByUsuarioIdAndAtivo(usuarioId, ativo);
        MotoristaResponseDTO response = CriptoUtils.decrypt(motorista.toResponseDTO(), motorista.getUsuario().getPersonalDataKeyVersion());
        return ResponseEntity.ok(response);
    }


    /**
     * Cria um novo motorista com base nos dados passados.
     * 
     * Retorna o motorista criado com status CREATED.
     * 
     * @param motoristaRequestDTO os dados do motorista a ser criado
     * @return o motorista criado com status CREATED
     */
    @PostMapping("/cadastro")
    public ResponseEntity<MotoristaResponseDTO> createMotorista(@RequestBody @Valid MotoristaRequestDTO motoristaRequestDTO) {
        Motorista savedMotorista = motoristaService.createMotorista(motoristaRequestDTO.toEntity(null));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMotorista.toResponseDTO());
    }

    /**
     * Atualiza um motorista com base nos dados passados.
     * 
     * Só permite que o motorista seja atualizado pelo seu próprio dono ou por um usuário com permissão de ADMIN.
     * 
     * Retorna o motorista atualizado com status OK.
     * 
     * @param usuarioId o id do usuário do motorista
     * @param motoristaRequestDTO os dados do motorista a ser atualizado
     * @return o motorista atualizado com status OK
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasRole('ADMIN')")
    @PatchMapping("/{usuarioId}")
    public ResponseEntity<MotoristaResponseDTO> updateMotorista(@PathVariable UUID usuarioId,  @RequestBody @Valid UsuarioPATCHRequestDTO motoristaRequestDTO) {
        Motorista updatedMotorista = motoristaService.updateMotorista(usuarioId, motoristaRequestDTO);
        return ResponseEntity.ok(new MotoristaResponseDTO(updatedMotorista));
    }

    /**
     * Deleta um motorista com base no seu id de usuário.
     * Só permite que o motorista seja deletado pelo seu próprio dono ou por um usuário com permissão de ADMIN.
     * O motorista é deletado logicamente, ou seja, o campo ativo é setado para false.
     * O motorista só pode ser deletado se não tiver reservas com status 'ativa' ou 'reservada'.
     * @param usuarioId o id do usuário do motorista
     * @return uma resposta sem conteúdo caso a exclusão seja realizada com sucesso
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> deleteMotorista(@PathVariable UUID usuarioId) {
        motoristaService.deleteByUsuarioId(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
