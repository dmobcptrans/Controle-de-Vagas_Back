package com.cptrans.petrocarga.controllers;

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
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.dto.VeiculoRequestDTO;
import com.cptrans.petrocarga.dto.VeiculoResponseDTO;
import com.cptrans.petrocarga.models.Veiculo;
import com.cptrans.petrocarga.services.VeiculoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

/**
 * Retorna uma lista de todos os veículos registrados.,
 * 
 * Só permite que os veículos sejam acessados por um usuário autenticado com permissão de ADMIN ou GESTOR.
 * 
 * @return lista de veículos encontrados com status ok
 * 
 */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping
    public ResponseEntity<List<VeiculoResponseDTO>> getAllVeiculos() {
        List<VeiculoResponseDTO> veiculos = veiculoService.findAll().stream()
                .map(VeiculoResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(veiculos);
    }

    /**
     * Retorna uma lista de todos os veículos registrados pelo usuário com o id de usuário passado como parâmetro.
     * Só permite que os veículos sejam acessados pelo próprio dono ou por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * @param usuarioId o id do usuário para buscar os veículos
     * @return lista de veiculos encontrados com status ok
     * 
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VeiculoResponseDTO>> getVeiculoByUsuarioId(@PathVariable UUID usuarioId) {
        List<VeiculoResponseDTO> veiculos = veiculoService.findByUsuarioId(usuarioId).stream().map(VeiculoResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(veiculos);
    }

    /**
     * Cria um novo veículo com base nos dados passados.
     * Só permite que os veículos sejam criados por um usuário com permissão de ADMIN ou pelo próprio dono (Motorista ou Empresa).
     * 
     * @param usuarioId o id do usuário para criar o veículo
     * @param veiculoRequestDTO os dados do veículo a ser criado
     * @return o veículo criado com status CREATED
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping({"/{usuarioId}"})
    public ResponseEntity<VeiculoResponseDTO> createVeiculo(@PathVariable UUID usuarioId, @RequestBody @Valid VeiculoRequestDTO veiculoRequestDTO) {
        Veiculo novoVeiculo = veiculoService.createVeiculo(veiculoRequestDTO.toEntity(), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoVeiculo.toResponseDTO());
    }

    /**
     * Retorna um veículo com base no id do veículo passado como parâmetro.
     * Só permite que os veículos sejam acessados pelo próprio dono (Motorista ou Empresa) ou por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * @param id o id do veículo para buscar
     * @return o veículo encontrado com status ok
     *
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> getVeiculoById(@PathVariable UUID id) {
        Veiculo veiculo = veiculoService.findById(id);
        return ResponseEntity.ok(veiculo.toResponseDTO());
    }

    /**
     * Atualiza um veículo com base no id do veículo passado como parâmetro e no id do usuário que está fazendo a requisição.
     * Só permite que os veículos sejam atualizados pelo próprio dono (Motorista ou Empresa) ou por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * @param id o id do veículo para atualizar
     * @param usuarioId o id do usuário que está fazendo a requisição
     * @param veiculoRequestDTO os dados do veículo a ser atualizado
     * @return o veículo atualizado com status ok
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}/{usuarioId}")
    public ResponseEntity<VeiculoResponseDTO> updateVeiculo(@PathVariable UUID id, @PathVariable UUID usuarioId, @RequestBody @Valid VeiculoRequestDTO veiculoRequestDTO) {
        Veiculo veiculo = veiculoService.updateVeiculo(id, usuarioId, veiculoRequestDTO);
        return ResponseEntity.ok(veiculo.toResponseDTO());
    }

    /**
     * Deleta um veículo com base no id do veículo passado como parâmetro.
     * Só permite que os veículos sejam deletados pelo próprio dono (Motorista ou Empresa) ou por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * @param id o id do veículo para deletar
     * @return uma resposta sem conteúdo com status NO_CONTENT
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','MOTORISTA', 'EMPRESA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeiculo(@PathVariable UUID id) {
        veiculoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}