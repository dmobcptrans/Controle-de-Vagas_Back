package com.cptrans.petrocarga.interfaces.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.cptrans.petrocarga.application.dto.PageResponseDTO;
import com.cptrans.petrocarga.application.dto.ReservaDTO;
import com.cptrans.petrocarga.application.dto.ReservaDetailedResponseDTO;
import com.cptrans.petrocarga.application.dto.ReservaPATCHRequestDTO;
import com.cptrans.petrocarga.application.dto.ReservaRequestDTO;
import com.cptrans.petrocarga.application.dto.ReservaResponseDTO;
import com.cptrans.petrocarga.application.usecase.MotoristaService;
import com.cptrans.petrocarga.application.usecase.ReservaService;
import com.cptrans.petrocarga.application.usecase.VagaService;
import com.cptrans.petrocarga.application.usecase.VeiculoService;
import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.entities.Veiculo;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;
    @Autowired
    private VagaService vagaService;
    @Autowired
    private MotoristaService motoristaService;
    @Autowired
    private VeiculoService veiculoService;

    /**
     * Retorna uma lista de reservas com base na lista de status e vaga ID informado.
     * 
     * @param status lista de status para filtrar as reservas
     * @param vagaId vaga ID para filtrar as reservas
     * @return lista de reservas com base na lista de status e vaga ID informado ou todas as reservas caso nenhum filtro seja informado.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> getReservas(@RequestParam(required = false) List<StatusReservaEnum> status, @RequestParam(required = false) UUID vagaId) {
        List<ReservaResponseDTO> reservas = reservaService.findAll(status, vagaId).stream()
                .map(ReservaResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservas);
    }

    /**
     * Retorna uma lista de reservas com base na lista de status, vaga ID e data informado.
     * 
     * Só permite que as reservas sejam acessadas por um usuário com permissão de ADMIN, GESTOR ou AGENTE.
     * 
     * @param vagaId vaga ID para filtrar as reservas
     * @param data data para filtrar as reservas
     * @param placa placa para filtrar as reservas
     * @param status lista de status para filtrar as reservas
     * @return lista de reservas com base na lista de status, vaga ID e data informado ou todas as reservas caso nenhum filtro seja informado.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping("/all/{vagaId}")
    public ResponseEntity<List<ReservaDTO>> getAllReservasWithFiltersByVaga(@PathVariable UUID vagaId,@RequestParam(required = false) LocalDate data, @RequestParam(required = false) String placa,@RequestParam(required = false) List<StatusReservaEnum> status) {
        Vaga vaga = vagaService.findById(vagaId);
        if(placa != null) {
            placa = placa.trim().toUpperCase();
            List<ReservaDTO> reservas = reservaService.getReservasByVagaIdDataAndPlaca(vaga.getId(), data, placa, status);
            return ResponseEntity.ok(reservas);
        }
        List<ReservaDTO> reservas = reservaService.getReservasByVagaIdAndData(vaga.getId(), data, status);
        return ResponseEntity.ok(reservas);
    }

    /**
     * Retorna uma lista de reservas com base na lista de status, data e placa informado.
     * 
     * Só permite que as reservas sejam acessadas por um usuário com permissão de ADMIN, GESTOR ou AGENTE.
     * 
     * @param data data para filtrar as reservas
     * @param placa placa para filtrar as reservas
     * @param status lista de status para filtrar as reservas
     * @return lista de reservas com base na lista de status, data e placa informado ou todas as reservas caso nenhum filtro seja informado.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE')")
    @GetMapping("/all")
    public ResponseEntity<List<ReservaDTO>> getAllReservasWithFilters(@RequestParam(required = false) LocalDate data, @RequestParam(required = false) String placa,@RequestParam(required = false) List<StatusReservaEnum> status) {
        if(placa != null) {
            placa = placa.trim().toUpperCase();
            List<ReservaDTO> reservas = reservaService.getAllReservasByDataAndPlaca( data, placa, status);
            return ResponseEntity.ok(reservas);
        }
        List<ReservaDTO> reservas = reservaService.getAllReservasByData( data, status);
        return ResponseEntity.ok(reservas);
    }
    
    /**
     * Retorna uma lista de reservas com base na placa informado.
     * 
     * Só permite que as reservas sejam acessadas por um usuário com permissão de ADMIN, GESTOR ou AGENTE.
     * 
     * @param placa placa para filtrar as reservas, a placa é obrigatória para acessar esse endpoint
     * @return lista de reservas com base na placa informado.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR','AGENTE')")
    @GetMapping("/placa")
    public ResponseEntity<List<ReservaDTO>> getAllReservasByPlaca(@RequestParam(required = true) String placa) {
        List<ReservaDTO> reservas = reservaService.getReservasAtivasByPlaca(placa.trim().toUpperCase());
        return ResponseEntity.ok(reservas);
    }

    /**
     * Retorna uma lista de intervalos de reservas que estão bloqueadas
     * em uma determinada vaga e data, com base no tipo de veículo informado.
     * Só permite que o endpoint seja acessado por um usuário com permissão de ADMIN, AGENTE, MOTORISTA ou EMPRESA.
     * 
     * @param vagaId vaga ID para filtrar as reservas
     * @param data data para filtrar as reservas
     * @param tipoVeiculo tipo de veiculo para filtrar as reservas
     * @return lista de intervalos de horários que estão bloqueadas em uma determinada vaga e data, com base no tipo de veiculo informado.
     */
    @PreAuthorize("hasAnyRole('ADMIN','AGENTE','MOTORISTA', 'EMPRESA')")
    @GetMapping("/bloqueios/{vagaId}")
    public ResponseEntity<List<ReservaService.Intervalo>> getIntervalosBloqueados(@PathVariable UUID vagaId, @RequestParam LocalDate data, @RequestParam TipoVeiculoEnum tipoVeiculo) {
        Vaga vaga = vagaService.findById(vagaId);
        List<ReservaService.Intervalo> intervalosBloqueados = reservaService.getIntervalosBloqueados(vaga, data, tipoVeiculo);
        return ResponseEntity.ok(intervalosBloqueados);
    }

    /**
     * Retorna uma reserva com base no seu id.
     * 
     * Só permite que a reserva seja acessada por um usuário autenticado com permissão de ADMIN, GESTOR, AGENTE, MOTORISTA ou EMPRESA.
     * 
     * @param id o id da reserva para buscar
     * @return a reserva com base no seu id
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'AGENTE', 'MOTORISTA', 'EMPRESA')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDetailedResponseDTO> getReservaById(@PathVariable UUID id) {
        // Busca a reserva e mantém as verificações de permissão no service
        Reserva reserva = reservaService.findById(id);
        // Converte para DTO detalhado que expõe nomes/placa para exibição amigável
        ReservaDetailedResponseDTO dto = new ReservaDetailedResponseDTO(reserva);
        return ResponseEntity.ok(dto);
    }

    /**
     * Retorna uma lista de reservas de um usuário com base no seu id de usuário.
     * Só permite que as reservas sejam acessadas pelo própio dono ou por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * 
     * @param usuarioId o id do usuário para buscar as reservas
     * @param status lista de status para filtrar as reservas
     * @return lista de reservas de um usuário com base no seu id de usuário filtradas ou não.
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PageResponseDTO> getReservasByUsuarioId(@PathVariable UUID usuarioId, @RequestParam(required = false) List<StatusReservaEnum> status, @RequestParam(defaultValue = "0") Integer numeroPagina, @RequestParam(defaultValue = "10") Integer tamanhoPagina) {
        Page<ReservaResponseDTO> reservas = reservaService.findByUsuarioId(usuarioId, status, numeroPagina, tamanhoPagina)
                .map(ReservaResponseDTO::new);
                
        return ResponseEntity.ok(new PageResponseDTO(reservas));
    }
    
    /**
     * Cria uma nova reserva com base nos dados passados.
     * Só permite que as reservas sejam acessadas por um usuário com permissão de ADMIN, MOTORISTA ou EMPRESA.
     * 
     * @param reservaRequestDTO os dados da reserva a ser criada
     * @return a reserva criada com status CREATED
     */
    @PreAuthorize("hasAnyRole('ADMIN','MOTORISTA', 'EMPRESA')")
    @PostMapping()
    public ResponseEntity<ReservaResponseDTO> createReserva(@RequestBody @Valid ReservaRequestDTO reservaRequestDTO) {
        Vaga vaga = vagaService.findById(reservaRequestDTO.getVagaId());
        Motorista motorista = motoristaService.findById(reservaRequestDTO.getMotoristaId());
        Veiculo veiculo = veiculoService.findById(reservaRequestDTO.getVeiculoId());
        Reserva novaReserva = reservaService.createReserva(reservaRequestDTO.toEntity(vaga, motorista, veiculo));
                                                                
        return ResponseEntity.status(HttpStatus.CREATED).body(novaReserva.toResponseDTO());

    }

    /**
     * Finaliza uma reserva com base no seu id, se a reserva já estiver como status 'ativa'.
     * Só permite que as reservas sejam acessadas por um usuário com permissão de ADMIN, GESTOR ou AGENTE.
     * 
     * @param id o id da reserva a ser finalizada
     * @return a reserva finalizada com status ok
     */
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','AGENTE')")
    @PostMapping("/{id}/finalizar-forcado")
    public ResponseEntity<ReservaDTO> finalizarReservaForcado(@PathVariable UUID id) {
        ReservaDTO reservaFinalizada = reservaService.finalizarForcado(id);
        return ResponseEntity.ok(reservaFinalizada);
    }

    /**
     * Realiza o check-in de uma reserva com base no seu id.
     * Só permite que as reservas sejam acessadas por um usuário com permissão de ADMIN, MOTORISTA ou EMPRESA.
     * Só o dono da reserva ou um usuário com permissão de ADMIN pode realizar o check-in, ou seja, um motorista só pode realizar o check-in de suas próprias reservas.
     * 
     * @param id o id da reserva a ser realizada o check-in
     * @return a reserva com status ok
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MOTORISTA', 'EMPRESA')")
    @PostMapping("/{id}/checkin")
    public ResponseEntity<ReservaResponseDTO> realizarCheckIn(@PathVariable UUID id) {
        Reserva reserva = reservaService.realizarCheckIn(id);
        return ResponseEntity.ok(reserva.toResponseDTO());
    }

    /**
     * Atualiza uma reserva com base no seu id e usuarioId.
     * 
     * Só permite que as reservas sejam acessadas pelo própio dono ou por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * 
     * @param id o id da reserva a ser atualizada
     * @param usuarioId o id do usuário para atualizar a reserva
     * @param reservaRequestDTO os dados da reserva a ser atualizados
     * @return a reserva atualizada com status CREATED
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @PatchMapping("/{id}/{usuarioId}")
    public ResponseEntity<ReservaResponseDTO> updateReserva(@PathVariable UUID id, @PathVariable UUID usuarioId, @RequestBody @Valid ReservaPATCHRequestDTO reservaRequestDTO) {
        Reserva reserva = reservaService.findById(id);
        Reserva reservaAtualizada = reservaService.atualizarReserva(reserva, usuarioId, reservaRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaAtualizada.toResponseDTO());
    }

    /**
     * Realiza o check-out de uma reserva com base no seu id.
     * Só permite que as reservas sejam acessadas pelo própio dono ou por um usuário autenticado.
     * 
     * @param id o id da reserva a ser realizada o check-out
     * @return a reserva com status CREATED
     */
    @PatchMapping("checkout/{id}")
    public ResponseEntity<ReservaResponseDTO> realizarCheckout(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @PathVariable UUID id ) {
        Reserva reservaAtualizada = reservaService.realizarCheckout(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaAtualizada.toResponseDTO());
    }

    /**
     * Cancela uma reserva com base no seu id e usuarioId.
     * 
     * Só permite que as reservas sejam acessadas pelo própio dono ou por um usuário autenticado com permissão de ADMIN ou GESTOR.
     * 
     * @param id o id da reserva a ser cancelada
     * @param usuarioId o id do usuário para cancelar a reserva
     * @return status ok sem conteúdo
     */
    @PreAuthorize("#usuarioId == authentication.principal.id or hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}/{usuarioId}")
    public ResponseEntity<Void> cancelarReserva(@PathVariable UUID id, @PathVariable UUID usuarioId) {
        
        reservaService.cancelarReserva(id, usuarioId);
        return ResponseEntity.noContent().build();
        
    }
}