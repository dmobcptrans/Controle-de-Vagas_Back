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

import com.cptrans.petrocarga.application.dto.VagaRequestDTO;
import com.cptrans.petrocarga.application.dto.VagaResponseDTO;
import com.cptrans.petrocarga.application.usecase.VagaService;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid; 

@RestController
@RequestMapping("/vagas")
public class VagaController {
    
    @Autowired
    private VagaService vagaService;

    /**
     * Retorna uma lista de todas as vagas registradas.
     * Só permite acesso por usuários autenticados com permissão de ADMIN, GESTOR, AGENTE, MOTORISTA ou EMPRESA.
     * 
     * @param status Opcional, se nulo, retorna todas as vagas. Caso contrário, retorna apenas as vagas com o status especificado.
     * 
     * @return Uma lista de vagas.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE','MOTORISTA','EMPRESA')")
    @GetMapping("/all")
    @Operation(
        summary = "Listar todas as vagas.",
        description = "Retorna uma lista de todas as vagas registradas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de vagas retornada com sucesso",
                        content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = VagaResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
        }
    )
    public ResponseEntity<List<VagaResponseDTO>> findAll(@RequestParam(required = false) StatusVagaEnum status) { 
        if(status != null) {
            List<VagaResponseDTO> vagas = vagaService.findAllByStatus(status).stream().map(VagaResponseDTO::new).toList();
            return ResponseEntity.ok(vagas);
        }
        List<VagaResponseDTO> vagas = vagaService.findAll().stream().map(VagaResponseDTO::new).toList();
        return ResponseEntity.ok(vagas);
    }

    /**
     * Retorna uma lista paginada de todas as vagas disponíveis com paginação e com filtros opcionais por status e nome da rua (logradouro).
     * 
     * Só permite acesso por usuários autenticados com permissão de ADMIN, GESTOR, AGENTE, MOTORISTA ou EMPRESA.
     * 
     * @param numeroPagina número da página a ser consultada (por padrão, começa em 0)
     * @param tamanhoPagina tamanho da página a ser consultada (por padrão, começa em 10)
     * @param ordenarPor campo para ordenar a lista de vagas (por padrão, utiliza "endereco.logradouro")
     * @param status status da vaga para filtrar as vagas (opcional)
     * @param logradouro nome da rua (logradouro) para filtrar as vagas (opcional)
     * 
     * @return Uma lista de vagas paginadas.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE','MOTORISTA','EMPRESA')")
    @GetMapping()
    @Operation(
        summary = "Listar todas as vagas com paginação",
        description = "Retorna uma lista paginada de todas as vagas disponíveis, com filtros opcionais por status e nome da rua (logradouro).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de vagas retornada com sucesso",
                            content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VagaResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
        }
    )
    public ResponseEntity<List<VagaResponseDTO>> findAllPaginadas(
            @RequestParam(defaultValue="0") Integer numeroPagina, 
            @RequestParam(defaultValue="10") Integer tamanhoPagina, 
            @RequestParam(defaultValue="endereco.logradouro") String ordenarPor, 
            @RequestParam(required = false) StatusVagaEnum status,
            
            @Parameter(description = "Filtrar vagas pelo nome da rua (logradouro). Busca parcial e case-insensitive.", example = "Rua do Imperador")
            @RequestParam(required = false) String logradouro) {
        

    	List<Vaga> vagasPaginadas = vagaService.findAllPaginadas(numeroPagina, tamanhoPagina, ordenarPor, status, logradouro);
        
        List<VagaResponseDTO> vagasDto = vagasPaginadas.stream()
                                                      .map(VagaResponseDTO::new)
                                                      .toList();
        
        return ResponseEntity.ok(vagasDto);
    }
    
    /**
     * Busca uma vaga pelo ID.
     * 
     * Só permite que a vaga seja acessada por um usuário autenticado com permissão de ADMIN, GESTOR, AGENTE, MOTORISTA ou EMPRESA.
     * 
     * @param id UUID da vaga a ser buscada.
     * @return Os detalhes da vaga encontrada ou um erro caso a vaga não seja encontrada.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE','MOTORISTA','EMPRESA')")
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar uma vaga pelo ID",
        description = "Retorna os detalhes de uma vaga específica identificada pelo seu UUID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Vaga encontrada com sucesso",
                        content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = VagaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vaga não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
        }
    )
    public ResponseEntity<VagaResponseDTO> findById(@Valid @PathVariable UUID id) {
        Vaga vaga = vagaService.findById(id);
        return ResponseEntity.ok(vaga.toResponseDTO());
    }

    /**
     * Cria uma nova vaga com base nos dados fornecidos no corpo da requisição.
     * Só permite que a vaga seja criada por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * 
     * @param vagaRequest dados necessários para criação de uma vaga
     * @return Os detalhes da vaga criada com sucesso ou um erro caso a vaga não seja criada.
     * 
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping()
    @Operation(
        summary = "Cadastrar uma nova vaga",
        description = "Cria uma nova vaga com base nos dados fornecidos no corpo da requisição.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados necessários para criação de uma vaga",
            required = true,
            content = @Content(schema = @Schema(implementation = VagaRequestDTO.class))
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Vaga criada com sucesso",
                        content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = VagaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos enviados"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
        }
    )
    public ResponseEntity<VagaResponseDTO> createVaga(@Valid @RequestBody VagaRequestDTO vagaRequest) {
        Vaga vaga = vagaService.createVaga(vagaRequest.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(new VagaResponseDTO(vaga));
    }
    

    /**
     * Deleta uma vaga específica identificada pelo seu id.
     * 
     * Só permite que a vaga seja deletada por um usuário autenticado com permissão de ADMIN.
     * 
     * @param id id da vaga a ser deletada
     * @return resposta sem conteúdo caso a vaga seja deletada com sucesso ou um erro caso a vaga não seja encontrada ou não seja deletada.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Deletar uma vaga pelo ID",
        description = "Remove uma vaga específica identificada pelo seu UUID.",
        parameters = {
            @Parameter(name = "id", description = "Identificador único da vaga", required = true,
                    example = "2cb9a7f0-4499-4531-9f67-3e1b6eaf1234")
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "Vaga deletada com sucesso (sem conteúdo)"),
            @ApiResponse(responseCode = "404", description = "Vaga não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
        }
    )
    public ResponseEntity<?> deleteById(@Valid @PathVariable UUID id) {
        vagaService.deleteById(id);
        return ResponseEntity.noContent().build(); 
    }
    
    /**
     * Atualiza parcialmente uma vaga com base nos dados fornecidos no corpo da requisição.
     * 
     * Só permite que a vaga seja atualizada por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * 
     * @param id id da vaga a ser atualizada
     * @param vagaRequest dados necessários para atualizar a vaga
     * @return Os detalhes da vaga atualizada com sucesso ou um erro caso a vaga não seja encontrada ou não seja atualizada.
     * 
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}")
    @Operation(
        summary = "Atualizar parcialmente uma vaga",
        description = "Atualiza apenas os campos enviados no corpo da requisição para a vaga especificada pelo ID.",
        parameters = {
            @Parameter(name = "id", description = "Identificador único da vaga", required = true,
                    example = "2cb9a7f0-4499-4531-9f67-3e1b6eaf1234")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Campos a serem atualizados na vaga",
            required = true,
            content = @Content(schema = @Schema(implementation = VagaRequestDTO.class))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Vaga atualizada com sucesso",
                        content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = VagaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos enviados"),
            @ApiResponse(responseCode = "404", description = "Vaga não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
        }
    )
    public ResponseEntity<VagaResponseDTO> updateById(@Valid @PathVariable UUID id,@Valid @RequestBody VagaRequestDTO vagaRequest) {
        System.out.println(vagaRequest.getStatus());
        Vaga vagaAtualizada = vagaService.updateById(id, vagaRequest.toEntity());
        return ResponseEntity.ok(new VagaResponseDTO(vagaAtualizada));
    }
}
