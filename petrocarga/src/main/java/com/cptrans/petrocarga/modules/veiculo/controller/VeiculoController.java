package com.cptrans.petrocarga.modules.veiculo.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.modules.veiculo.dto.request.VeiculoFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.request.VeiculoRequestDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculo.service.VeiculoService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Veiculos", description = "Endpoints para gerenciamento de veículos")
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final VeiculoMapper veiculoMapper;

    //GET /veiculos
    @Operation(
        summary = "Listar veículos",
        description = "Listagem de veículos com paginação e filtros"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping
    public ResponseEntity<PageResponseDTO> getAllVeiculos(
        @Parameter(description = "Placa do veículo")
        @RequestParam(required = false) String placa,

        @Parameter(description = "Marca do veículo")
        @RequestParam(required = false) String marca,

        @Parameter(description = "Modelo do veículo")
        @RequestParam(required = false) String modelo,

        @Parameter(description = "Tipo do veículo")
        @RequestParam(required = false) TipoVeiculoEnum tipo,

        @Parameter(description = "ID do usuário associado ao veículo")
        @RequestParam(required = false) UUID usuarioId,

        @Parameter(description = "Telefone do usuário associado ao veículo")
        @RequestParam(required = false) String telefoneUsuario,

        @Parameter(description = "CPF do proprietário do veículo")
        @RequestParam(required = false) String cpfProprietario,

        @Parameter(description = "CNPJ do proprietário do veículo")
        @RequestParam(required = false) String cnpjProprietario,

        @Parameter(description = "Status do veículo (ativo/inativo)")
        @RequestParam(required = false) Boolean ativo,

        @Parameter(description = "Número da página", example = "0")
        @RequestParam(defaultValue = "0") int pagina,

        @Parameter(description = "Quantidade de registros por página", example = "10")
        @RequestParam(defaultValue = "10") int tamanhoPagina,

        @Parameter(description = "Ordem da listagem", example = "ASC")
        @RequestParam(defaultValue = "ASC") OrdemEnum ordem
    ) {
        VeiculoFiltrosRequestDTO filtros = new VeiculoFiltrosRequestDTO(placa, marca, modelo, tipo, usuarioId, telefoneUsuario, cpfProprietario, cnpjProprietario, ativo);
        return ResponseEntity.ok(veiculoService.findAll(filtros, pagina, tamanhoPagina, ordem));
    }

    //GET /veiculos/usuario/{usuarioId}
    @Operation(
        summary = "Listar todos os veículos ativos de um usuário",
        description = "Listagem de veículos ativos à partir de um ID de usuário(ativo) sem paginação"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VeiculoResponseDTO>> getVeiculosByUsuarioId(
            @Parameter(description = "ID do usuário associado ao veículo")
            @PathVariable UUID usuarioId
        ) {
        List<VeiculoResponseDTO> veiculos = veiculoService.findAtivosByUsuarioId(usuarioId).stream()
                .map(veiculo -> veiculoMapper.toResponse(veiculo))
                .collect(Collectors.toList());
        return ResponseEntity.ok(veiculos);
    }

    //POST /veiculos/{usuarioId}
    @Operation(
        summary = "Criar veículo",
        description = "Criação de um veículo para um usuário"
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping({"/{usuarioId}"})
    public ResponseEntity<VeiculoResponseDTO> createVeiculo(
            @Parameter(description = "ID do usuário que terá o novo veículo")
            @PathVariable UUID usuarioId,
        
            @Parameter(description = "Informações do veículo")
            @Valid @RequestBody VeiculoRequestDTO veiculoRequestDTO
        ) {
        Veiculo novoVeiculo = veiculoService.createVeiculo(veiculoRequestDTO.toEntity(), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoMapper.toResponse(novoVeiculo));
    }

    //GET /veiculos/{id}
    @Operation(
        summary = "Visualizar veículo",
        description = "Retorna um veículo com base no id do veículo"
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> getVeiculoById(
            @Parameter(description = "ID do veículo")
            @PathVariable UUID id
        ) {
        Veiculo veiculo = veiculoService.findById(id);
        return ResponseEntity.ok(veiculoMapper.toResponse(veiculo));
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
        return ResponseEntity.ok(veiculoMapper.toResponse(veiculo));
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