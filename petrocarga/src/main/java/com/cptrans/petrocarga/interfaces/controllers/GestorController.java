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

import com.cptrans.petrocarga.application.dto.GestorFiltrosDTO;
import com.cptrans.petrocarga.application.dto.GestorRequestDTO;
import com.cptrans.petrocarga.application.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.application.dto.UsuarioResponseDTO;
import com.cptrans.petrocarga.application.usecase.GestorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/gestores")
public class GestorController {
    @Autowired
    private GestorService gestorService;

    /**
     * Retorna uma lista de gestores com base nos filtros passados.
     *
     * Os filtros são: nome, telefone, email, ativo.
     * Se nenhum filtro for passado, então retorna uma lista com todos gestores.
     *
     * @param nome o nome do gestor
     * @param telefone o telefone do gestor
     * @param email o email do gestor
     * @param ativo se o gestor está ativo
     * @return uma lista de gestores com base nos filtros passados ou todos gestores se nenhum filtro for passado.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UsuarioResponseDTO>> getAllGestores(@RequestParam(required = false) String nome, @RequestParam(required = false) String telefone, @RequestParam(required = false) String email, @RequestParam(required = false) Boolean ativo) {
        if(nome != null || telefone != null || email != null || ativo != null) {
            GestorFiltrosDTO filtros = new GestorFiltrosDTO(nome, telefone, email, ativo);
            return ResponseEntity.ok(gestorService.findAllWithFiltros(filtros).stream()
                    .map(gestor -> gestor.toResponseDTO())
                    .toList());
        }

        List<UsuarioResponseDTO> gestores = gestorService.findAll().stream()
                .map(gestor -> gestor.toResponseDTO())
                .toList();
        return ResponseEntity.ok(gestores);
    }

    /**
     * Retorna um gestor com base no seu id de usuário.
     * Só permite que o gestor seja acessado pelo seu próprio dono ou por um usuário com permissão de ADMIN.
     * @param usuarioId o id do usuário do gestor
     * @return o gestor com base no seu id de usuário
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponseDTO> getGestorById(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(gestorService.findByUsuarioId(usuarioId).toResponseDTO());
    }

    /**
     * Cria um novo gestor com base nos dados passados.
     * Só permite que o gestor seja criado por um usuário com permissão de ADMIN.
     * @param gestorRequestDTO o objeto com os dados do gestor
     * @return o objeto criado com base nos dados do gestor
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<UsuarioResponseDTO> createGestor(@RequestBody @Valid GestorRequestDTO gestorRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorService.createGestor(gestorRequestDTO.toEntity()).toResponseDTO());
    }

    /**
     * Atualiza um gestor com base nos dados passados.
     * Só permite que o gestor seja atualizado pelo seu próprio dono ou por um usuário com permissão de ADMIN.
     * @param usuarioId o id do usuário do gestor
     * @param gestorRequestDTO o objeto com os dados do gestor
     * @return o objeto atualizado com base nos dados do gestor
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasRole('ADMIN')")
    @PatchMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponseDTO> updateGestor(@PathVariable UUID usuarioId, @RequestBody @Valid UsuarioPATCHRequestDTO gestorRequestDTO) {
        return ResponseEntity.ok(gestorService.updateGestor(usuarioId, gestorRequestDTO).toResponseDTO());
    }

    /**
     * Deleta um gestor com base no seu id de usuário.
     * Só permite que o gestor seja deletado pelo seu próprio dono ou por um usuário com permissão de ADMIN.
     * @param usuarioId o id do usuário do gestor
     * @return uma resposta sem conteúdo caso a exclusão seja realizada com sucesso
     */

    @PreAuthorize("#usuarioId == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> deleteGestor(@PathVariable UUID usuarioId) {
        gestorService.deleteByUsuarioId(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
