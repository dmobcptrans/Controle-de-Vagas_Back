package com.cptrans.petrocarga.modules.reserva.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.cptrans.petrocarga.config.swagger.response.PatchResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.modules.reserva.dto.mapper.ReservaMapper;
import com.cptrans.petrocarga.modules.reserva.dto.request.ReservaPATCHRequestDTO;
import com.cptrans.petrocarga.modules.reserva.dto.request.ReservaRequestDTO;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDetailedResponseDTO;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaResponseDTO;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.reserva.service.ReservaService;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.service.VagaService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Reservas", description = "Endpoints para gerenciamento de reservas")
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;
    private final VagaService vagaService;
    private final UsuarioUtils usuarioUtils;
    private final ReservaMapper reservaMapper;

    // GET /reservas/all
    @Operation(
        summary = "Listar todas as reservas",
        description = "Retorna uma lista de reservas opcionalmente filtradas com base nos parâmetros informados."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping("/all")
    public ResponseEntity<List<ReservaDTO>> getAllReservas(
        @Parameter(description = "Lista de Status da reserva")
        @RequestParam(required = false) List<StatusReservaEnum> status, 
        
        @Parameter(description = "ID da vaga")
        @RequestParam(required = false) UUID vagaId, 
        
        @Parameter(description = "Placa do veículo")
        @RequestParam(required = false) String placa, 
        
        @Parameter(description = "Data da reserva")
        @RequestParam(required = false) LocalDate data, 
        
        @Parameter(description = "ID do usuário")
        @RequestParam(required = false) UUID usuarioId, 
        
        @Parameter(description = "Mês da reserva")
        @RequestParam(required = false) Integer mes, 
        
        @Parameter(description = "Ano da reserva")
        @RequestParam(required = false) Integer ano
    ) {
        DateUtils.validarFiltrosData(data, mes, ano);

        List<ReservaDTO> reservas = reservaService.findAll(status, vagaId, placa, data, usuarioId, mes, ano);
        return ResponseEntity.ok(reservas);
      
    }

    // GET /reservas/all/{vagaId}
    @Operation(
        summary = "Listar todas as reservas de uma vaga",
        description = "Retorna uma lista de reservas de uma vaga com base no ID da vaga e opcionalmente filtradas com base nos parâmetros informados."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping("/all/{vagaId}")
    public ResponseEntity<List<ReservaDTO>> getAllReservasWithFiltersByVaga(
        @Parameter(description = "ID da vaga")
        @PathVariable UUID vagaId,
        
        @Parameter(description = "Data da reserva")
        @RequestParam(required = false) LocalDate data, 
        
        @Parameter(description = "Placa do veículo")
        @RequestParam(required = false) String placa,
        
        @Parameter(description = "Lista de Status da reserva")
        @RequestParam(required = false) List<StatusReservaEnum> status
    ) {
        Vaga vaga = vagaService.findById(vagaId);
        if (placa != null) {
            placa = placa.trim().toUpperCase();
            List<ReservaDTO> reservas = reservaService.getReservasByVagaIdDataAndPlaca(vaga.getId(), data, placa, status);
            return ResponseEntity.ok(reservas);
        }
        List<ReservaDTO> reservas = reservaService.getReservasByVagaIdAndData(vaga.getId(), data, status);
        return ResponseEntity.ok(reservas);
    }

    // GET /reservas/placa
    @Operation(
        summary = "Listar todas as reservas ativas de um veículo",
        description = "Retorna uma lista de reservas de um veículo com status ('reservada' ou 'ativa') com base na placa informada."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE')")
    @GetMapping("/placa")
    public ResponseEntity<List<ReservaDTO>> getAllReservasByPlaca(
        @Parameter(description = "Placa do veículo")
        @RequestParam(required = true) String placa
    ) {
        List<ReservaDTO> reservas = reservaService.getReservasAtivasByPlaca(placa);
        return ResponseEntity.ok(reservas);
    }

    // GET /reservas/bloqueios/{vagaId}
    @Operation(
        summary = "Listar todos os intervalos bloqueados de uma vaga",
        description = "Retorna uma lista de intervalos bloqueados de uma vaga com base no ID da vaga, a data da reserva e o tipo de veículo."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN','AGENTE','MOTORISTA', 'EMPRESA')")
    @GetMapping("/bloqueios/{vagaId}")
    public ResponseEntity<List<ReservaService.Intervalo>> getIntervalosBloqueados(
        @Parameter(description = "ID da vaga")
        @PathVariable UUID vagaId, 
        
        @Parameter(description = "Data da reserva")
        @RequestParam LocalDate data, 
        
        @Parameter(description = "Tipo de veículo")
        @RequestParam TipoVeiculoEnum tipoVeiculo
    ) {
        List<ReservaService.Intervalo> intervalosBloqueados = reservaService.getIntervalosBloqueados(vagaId, data, tipoVeiculo);
        return ResponseEntity.ok(intervalosBloqueados);
    }

    // GET /reservas/{id}
    @Operation(
        summary = "Buscar reserva por ID",
        description = "Retorna os dados de uma reserva a partir do ID da reserva."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDetailedResponseDTO> getReservaById(
        @Parameter(description = "ID da reserva")
        @PathVariable UUID id
    ) {
        Reserva reserva = reservaService.findById(id);
        ReservaDetailedResponseDTO dto = reservaMapper.toDetailedResponse(reserva);
        return ResponseEntity.ok(dto);
    }

    // GET /reservas/usuario/{usuarioId}
    @Operation(
        summary = "Listar todas as reservas de um usuário",
        description = "Retorna uma lista paginada de reservas de um usuário com base no ID do usuário e nos parâmetros informados."
    )
    @GetResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PageResponseDTO> getReservasByUsuarioIdOrMotoristaId(
        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId, 
        
        @Parameter(description = "Lista de Status da reserva")
        @RequestParam(required = false) List<StatusReservaEnum> status, 
        
        @Parameter(description = "Número da pagina")
        @RequestParam(defaultValue = "0") Integer numeroPagina, 
        
        @Parameter(description = "Quantidade de registros por pagina")
        @RequestParam(defaultValue = "10") Integer tamanhoPagina
    ) {
        Page<ReservaResponseDTO> reservas = reservaService.findByCriadoPorIdOrMotoristaId(usuarioId, status, numeroPagina, tamanhoPagina)
                .map((r) -> {
                    Usuario criadoPor = r.getCriadoPor();
                    String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
                    return reservaMapper.toResponse(r, cpfOrCnpjCriador);
                });
                
        return ResponseEntity.ok(new PageResponseDTO(reservas));
    }
    
    // POST /reservas
    @Operation(
        summary = "Criar reserva",
        description = "Cria uma nova reserva com base nos parâmetros informados."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN','MOTORISTA', 'EMPRESA')")
    @PostMapping()
    public ResponseEntity<ReservaResponseDTO> createReserva(
        @Parameter(description = "Dados da reserva")
        @Valid @RequestBody ReservaRequestDTO request
    ) {
        Reserva novaReserva = reservaService.createReserva(request);
        Usuario criadoPor = novaReserva.getCriadoPor();
        String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaMapper.toResponse(novaReserva, cpfOrCnpjCriador));
    }

    // POST /reservas/{id}/finalizar-forcado
    @Operation(
        summary = "Finalizar reserva",
        description = "Finaliza uma reserva com base no ID da reserva."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','AGENTE')")
    @PostMapping("/{id}/finalizar-forcado")
    public ResponseEntity<ReservaDTO> finalizarReservaForcado(
        @Parameter(description = "ID da reserva")
        @PathVariable UUID id
    ) {
        ReservaDTO reservaFinalizada = reservaService.finalizarForcado(id);
        return ResponseEntity.ok(reservaFinalizada);
    }

    // POST /reservas/{id}/checkin
    @Operation(
        summary = "Realizar check-in",
        description = "Realiza o check-in de uma reserva com base no ID da reserva."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasAnyRole('ADMIN', 'MOTORISTA', 'EMPRESA')")
    @PostMapping("/{id}/checkin")
    public ResponseEntity<ReservaResponseDTO> realizarCheckIn(
        @Parameter(description = "ID da reserva")
        @PathVariable UUID id
    ) {
        Reserva reserva = reservaService.realizarCheckIn(id);
        Usuario criadoPor = reserva.getCriadoPor();
        String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
        return ResponseEntity.ok(reservaMapper.toResponse(reserva, cpfOrCnpjCriador));
    }

    
    //PATCH /reservas/{id}/{usuarioId}
    @Operation(
        summary = "Atualizar reserva",
        description = "Atualiza uma reserva com base no ID da reserva, ID do usuário e nos parâmetros informados."
    )
    @PatchResponses
    @DefaultResponses
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}/{usuarioId}")
    public ResponseEntity<ReservaResponseDTO> updateReserva(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated, 
        
        @Parameter(description = "ID da reserva")
        @PathVariable UUID id, 
        
        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId, 
        
        @Parameter(description = "Dados da reserva")
        @Valid @RequestBody ReservaPATCHRequestDTO reservaRequestDTO
    ) {
        Reserva reservaAtualizada = reservaService.atualizarReserva(userAuthenticated, id, usuarioId, reservaRequestDTO);
        Usuario criadoPor = reservaAtualizada.getCriadoPor();
        String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaMapper.toResponse(reservaAtualizada, cpfOrCnpjCriador));
    }

    //PATCH /reservas/{id}/checkout
    @Operation(
        summary = "Realizar checkout",
        description = "Realiza o checkout de uma reserva com base no ID da reserva."
    )
    @PatchResponses
    @DefaultResponses
    @PatchMapping("checkout/{id}")
    public ResponseEntity<ReservaResponseDTO> realizarCheckout(
        @Parameter(description = "Usuário autenticado")
        @AuthenticationPrincipal UserAuthenticated userAuthenticated, 
        
        @Parameter(description = "ID da reserva")
        @PathVariable UUID id 
    ) {
        Reserva reservaAtualizada = reservaService.realizarCheckout(id);
        Usuario criadoPor = reservaAtualizada.getCriadoPor();
        String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaMapper.toResponse(reservaAtualizada, cpfOrCnpjCriador));
    }

    //DELETE /reservas/{id}/{usuarioId}
    @Operation(
        summary = "Cancelar reserva",
        description = "Cancela uma reserva com base no ID da reserva e ID do usuário."
    )
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}/{usuarioId}")
    public ResponseEntity<Void> cancelarReserva(
        @Parameter(description = "ID da reserva")
        @PathVariable UUID id, 
        
        @Parameter(description = "ID do usuário")
        @PathVariable UUID usuarioId
    ) {
        reservaService.cancelarReserva(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}