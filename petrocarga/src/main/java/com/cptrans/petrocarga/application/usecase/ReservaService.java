package com.cptrans.petrocarga.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.ReservaDTO;
import com.cptrans.petrocarga.application.dto.ReservaPATCHRequestDTO;
import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.entities.Notificacao;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.entities.Veiculo;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.enums.TipoNotificacaoEnum;
import com.cptrans.petrocarga.domain.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.domain.repositories.ReservaRepository;
import com.cptrans.petrocarga.infrastructure.scheduler.handlers.NotificacaoSchedulerService;
import com.cptrans.petrocarga.infrastructure.scheduler.handlers.ReservaSchedulerService;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.ReservaUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private MotoristaService motoristaService;
    @Autowired
    private VeiculoService veiculoService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private VagaService vagaService;
    @Autowired
    private ReservaRapidaService reservaRapidaService;
    @Autowired
    private NotificacaoService notificacaoService;
    @Autowired
    private ReservaUtils reservaUtils;
    @Autowired
    private ReservaSchedulerService reservaSchedulerService;
    @Autowired
    private NotificacaoSchedulerService notificacaoSchedulerService;
    // @Autowired
    // private DisponibilidadeVagaService disponibilidadeVagaService;


    public List<Reserva> findAll(List<StatusReservaEnum> status, UUID vagaId) {
        if(status == null) status = new ArrayList<>();
        if(vagaId != null) {
            return findByVagaId(vagaId, status);
        }
        if(!status.isEmpty()) {
            return findByStatus(status);
        }
        return reservaRepository.findAll();
    }

    public List<Reserva> findAllByData(LocalDate data, List<StatusReservaEnum> status, UUID vagaId) {
        List<Reserva> reservas = findAll(status, vagaId);
        if(reservas.isEmpty()) return reservas;
        if(data != null) {
            return reservas.stream().filter(reserva -> DateUtils.toLocalDateInBrazil(reserva.getInicio()).equals(data) || DateUtils.toLocalDateInBrazil(reserva.getFim()).equals(data)).toList();
        }
        return reservas;
    }

    public List<Reserva> findByStatus(List<StatusReservaEnum> status) {
        return reservaRepository.findByStatusIn(status);
    }

    public List<Reserva> findByVagaId(UUID vagaId, List<StatusReservaEnum> status) {
        Vaga vaga = vagaService.findById(vagaId);
        if(status == null) status = new ArrayList<>();
        if(!status.isEmpty()) {
            return reservaRepository.findByVagaAndStatusIn(vaga, status);
        }
        return reservaRepository.findByVaga(vaga);
    }
    
    public List<Reserva> findByVagaIdAndDataAndStatusIn(UUID vagaId, LocalDate data, List<StatusReservaEnum> status) {
        Vaga vaga = vagaService.findById(vagaId);
        List<Reserva> reservas = reservaRepository.findByVaga(vaga);
        if (status == null) status = new ArrayList<>();
        if(!status.isEmpty()) {
            reservas = reservaRepository.findByVagaAndStatusIn(vaga, status);
        }
        if(data != null) {
            return reservas.stream().filter(reserva -> DateUtils.toLocalDateInBrazil(reserva.getInicio()).equals(data)).toList();
        }
        return reservas;
    }

    public Reserva findById(UUID reservaId) {
        // Substituí o método para usar o novo método com fetch joins
        Reserva reserva = reservaRepository.findByIdWithJoins(reservaId);
        UserAuthenticated usuarioLogado = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> authorities = usuarioLogado.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        if(authorities.contains(PermissaoEnum.MOTORISTA.getRole())) {
            Motorista motorista = motoristaService.findByUsuarioId(usuarioLogado.id());
            if(!reserva.getMotorista().getId().equals(motorista.getId())) {
                throw new IllegalArgumentException("Usuário não pode ver as reservas de outro usuário.");   
            }
        }
        if(authorities.contains(PermissaoEnum.EMPRESA.getRole())) {
            Veiculo veiculoReserva = veiculoService.findById(reserva.getVeiculo().getId());
            if(!reserva.getCriadoPor().getId().equals(usuarioLogado.id()) || !reserva.getVeiculo().getId().equals(veiculoReserva.getId())) {
                throw new IllegalArgumentException("Usuário não pode ver as reservas de outro usuário.");   
            }
        }
        return reserva;
    }

    public List<Reserva> findByUsuarioId(UUID usuarioId, List<StatusReservaEnum> status) {
        Usuario usuario = usuarioService.findById(usuarioId);
        if(status == null ) status = new ArrayList<>();
        if(!status.isEmpty()) {
            return reservaRepository.findByCriadoPorAndStatusIn(usuario, status);
        }
        return reservaRepository.findByCriadoPor(usuario);
    }

    public Reserva createReserva(Reserva novaReserva) {
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuarioLogado = usuarioService.findById(userAuthenticated.id());
        novaReserva.setCriadoPor(usuarioLogado);
        checarExcecoesReserva(novaReserva, novaReserva.getCriadoPor(), novaReserva.getMotorista(), novaReserva.getVeiculo(), ReservaUtils.METODO_POST);
        Reserva reservaSalva = reservaRepository.save(novaReserva);
        try {
            reservaSchedulerService.agendarFinalizacaoReserva(reservaSalva.toReservaDTO());
            reservaSchedulerService.agendarFinalizacaoNoShow(reservaSalva.toReservaDTO());
            notificacaoSchedulerService.agendarNotificacaoCheckInDisponivel(reservaSalva.getMotorista().getUsuario().getId(), reservaSalva.getId(),reservaSalva.getInicio());
            notificacaoSchedulerService.agendarNotificacaoFimProximo(reservaSalva.getMotorista().getUsuario().getId(), reservaSalva.getId(), reservaSalva.getFim());

        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar finalização da reserva: " + e.getMessage());
        }
        return reservaSalva;
    }

    public void deleteById(UUID id) {
        reservaRepository.deleteById(id);
    }

    public void checarExcecoesReserva(Reserva novaReserva, Usuario usuarioLogado, Motorista motoristaDaReserva, Veiculo veiculoDaReserva, String metodoChamador) {
        Vaga vagaNovaReserva = novaReserva.getVaga();
        List<StatusReservaEnum> listaStatus = new ArrayList<>(List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA));
        List<Reserva> reservasAtivasNaVaga = reservaRepository.findByVagaAndStatusIn(vagaNovaReserva, listaStatus);
        List<ReservaRapida> reservasRapidasAtivasNaVaga = reservaRapidaService.findByVagaAndStatusIn(vagaNovaReserva, listaStatus);
        ReservaUtils.validarTempoMaximoReserva(novaReserva.toReservaDTO(), vagaNovaReserva);
        reservaUtils.validarEspacoDisponivelNaVaga(novaReserva, usuarioLogado, reservasAtivasNaVaga, reservasRapidasAtivasNaVaga, metodoChamador);
        reservaUtils.validarPermissoesReserva(usuarioLogado, motoristaDaReserva, veiculoDaReserva);
    }

    public List<ReservaDTO> getReservasByVagaAndData(Vaga vaga, LocalDate data, List<StatusReservaEnum> status) {
        List<Reserva> reservas = findByVagaIdAndDataAndStatusIn(vaga.getId(), data, status);
        List<ReservaRapida> reservasRapidas = reservaRapidaService.findByVagaAndDataAndStatusIn(vaga, data, status);
        List<ReservaDTO> listaFinalReservas = ReservaUtils.juntarReservas(reservas, reservasRapidas);
        return listaFinalReservas;
    }

    public List<ReservaDTO> getAllReservasByData(LocalDate data, List<StatusReservaEnum> status) {
        List<Reserva> reservas = findAllByData(data, status, null);
        List<ReservaRapida> reservasRapidas = reservaRapidaService.findAllByData(data, status);
        List<ReservaDTO> listaFinalReservas = ReservaUtils.juntarReservas(reservas, reservasRapidas);
        return listaFinalReservas;
    }

    public List<ReservaDTO> getReservasByVagaDataAndPlaca(Vaga vaga, LocalDate data, String placa, List<StatusReservaEnum> status) {
        List<ReservaDTO> reservas = getReservasByVagaAndData(vaga, data, status);
        return reservas.stream()
                .filter(r -> r.getPlacaVeiculo().equalsIgnoreCase(placa))
                .toList();
    }

    public List<ReservaDTO> getAllReservasByDataAndPlaca(LocalDate data, String placa, List<StatusReservaEnum> status) {
        List<ReservaDTO> reservas = getAllReservasByData( data, status);
        return reservas.stream()
                .filter(r -> r.getPlacaVeiculo().equalsIgnoreCase(placa))
                .toList();
    }

    public List<ReservaDTO> getReservasAtivasByPlaca(String placa){
        List<Reserva> reservasPorPlaca = reservaRepository.findByVeiculoPlacaIgnoringCaseAndStatusIn(placa, new ArrayList<>(List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA)));
        List<ReservaRapida> reservasRapidasPorPlaca = reservaRapidaService.findByPlaca(placa != null ? placa.trim().toUpperCase() : null);
        List<ReservaDTO> listaReservasAtivasPorPlaca = ReservaUtils.juntarReservas(reservasPorPlaca, reservasRapidasPorPlaca);
        return listaReservasAtivasPorPlaca;
    }

    public List<Intervalo> getIntervalosBloqueados(Vaga vaga, LocalDate data, TipoVeiculoEnum tipoVeiculo  ) {
        int capacidadeTotal = vaga.getComprimento();
        int comprimentoVeiculoDesejado = tipoVeiculo.getComprimento();

        List<ReservaDTO> reservas = getReservasByVagaAndData(vaga, data, new ArrayList<>(List.of(StatusReservaEnum.RESERVADA, StatusReservaEnum.ATIVA)));
        if (reservas.isEmpty()) {
            return List.of(); // nada reservado → nenhum bloqueio
        }
        // 1) coleta todos os pontos de corte
        TreeSet<Instant> pontos = new TreeSet<>();
        reservas.forEach(r -> {
            pontos.add(r.getInicio().toInstant());
            pontos.add(r.getFim().toInstant());
        });

        List<Instant> timeline = new ArrayList<>(pontos);

        // 2) calcula segmentos e marca os bloqueados
        List<Intervalo> intervalosBloqueados = new ArrayList<>();
        Intervalo atual = null;

        for (int i = 0; i < timeline.size() - 1; i++) {
            Instant inicio = timeline.get(i);
            Instant fim = timeline.get(i + 1);

            if (inicio.equals(fim)) continue;

            int ocupacaoAtual = 0;

            for (ReservaDTO res : reservas) {
                boolean sobrepoe = res.getInicio().toInstant().isBefore(fim)
                        && res.getFim().toInstant().isAfter(inicio);

                if (sobrepoe) {
                    ocupacaoAtual += res.getTamanhoVeiculo();
                }
            }

            int espacoRestante = capacidadeTotal - ocupacaoAtual;
            boolean cabe = espacoRestante >= comprimentoVeiculoDesejado;

            if (!cabe) {
                OffsetDateTime dtoIni = OffsetDateTime.ofInstant(inicio, ZoneOffset.of("-03:00"));
                OffsetDateTime dtoFim = OffsetDateTime.ofInstant(fim, ZoneOffset.of("-03:00"));

                if (atual == null) {
                    atual = new Intervalo(dtoIni, dtoFim);
                } else {
                    atual.setFim(dtoFim);
                }
            } else {
                if (atual != null) {
                    intervalosBloqueados.add(atual);
                    atual = null;
                }
            }
        }

        if (atual != null) intervalosBloqueados.add(atual);

        return intervalosBloqueados;
}

/* Auxiliares */

public static class Intervalo {
    private OffsetDateTime inicio;
    private OffsetDateTime fim;

    public Intervalo(OffsetDateTime inicio, OffsetDateTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public OffsetDateTime getInicio() { return inicio; }
    public OffsetDateTime getFim() { return fim; }
    public void setInicio(OffsetDateTime inicio) { this.inicio = inicio; }
    public void setFim(OffsetDateTime fim) { this.fim = fim; }
}

    /**
     * Finaliza uma reserva de forma forçada por um AGENTE/ADMIN ou pelo job automático.
     * Regras:
     *  - Reserva deve existir e estar ATIVA
     *  - A finalização só é bloqueada se ainda não começou
     *  - Usuário autenticado deve possuir ROLE_AGENTE ou ROLE_ADMIN (garantido por @PreAuthorize no controller)
     * Efeitos:
     *  - Atualiza status para REMOVIDA
     *  - Não altera o campo "fim" para evitar impacto em relatórios existentes
     */
    public ReservaDTO finalizarForcado(UUID reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        Optional<ReservaRapida> reservaRapida = reservaRapidaService.findById(reservaId);
        ReservaDTO reservaDTO = new ReservaDTO();

        if (!reserva.isPresent() && !reservaRapida.isPresent()) throw new EntityNotFoundException("Reserva não encontrada.");
       
        if(reserva.isPresent()){
            if (!StatusReservaEnum.RESERVADA.equals(reserva.get().getStatus()) && !StatusReservaEnum.ATIVA.equals(reserva.get().getStatus())) {
                throw new IllegalStateException("Só é possivel finalizar uma reserva com status 'RESERVADA' ou 'ATIVA'.");
            }
            reserva.get().setStatus(StatusReservaEnum.REMOVIDA);
            reservaRepository.save(reserva.get());
            reservaDTO = new ReservaDTO(reserva.get());
        }
        if (reservaRapida.isPresent()){
            if (!StatusReservaEnum.RESERVADA.equals(reservaRapida.get().getStatus()) && !StatusReservaEnum.ATIVA.equals(reservaRapida.get().getStatus())) {
                throw new IllegalStateException("Só é possivel finalizar uma reserva com status 'RESERVADA' ou 'ATIVA'.");
            }
            reservaRapida.get().setStatus(StatusReservaEnum.REMOVIDA);
            reservaRapidaService.save(reservaRapida.get());
            reservaDTO = new ReservaDTO(reservaRapida.get());
        }

        notificacaoService.sendNotificationToUsuarioBySystem(reservaDTO.getCriadoPor().getId(), new Notificacao("Checkout Forçado","Sua reserva foi removida por um gestor. Realize uma nova reserva se necessário", TipoNotificacaoEnum.RESERVA),null);
        
        if (reserva.isPresent()){
            try {
                notificacaoSchedulerService.cancelarSchedulerCheckIn(reserva.get().getMotorista().getUsuario().getId(), reserva.get().getId());
                notificacaoSchedulerService.cancelarSchedulerFimProximo(reserva.get().getMotorista().getUsuario().getId(), reserva.get().getId());
            } catch (Exception e) {
                throw new RuntimeException("Erro ao cancelar schedulers no checkout forçado: " + e.getMessage());
            }
        }
        
        return reservaDTO;

    }

    /**
     * Realiza o check-in de uma reserva.
     * Regras:
     *  - Reserva deve existir e estar ATIVA
     *  - Não pode já ter feito check-in
     *  - Check-in só é permitido dentro do período da reserva (ou poucos minutos antes)
     */
    public Reserva realizarCheckIn(UUID reservaId) {
        Reserva reserva = findById(reservaId);

        if (!StatusReservaEnum.RESERVADA.equals(reserva.getStatus())) {
            throw new IllegalStateException("Reserva não está ativa.");
        }

        if (Boolean.TRUE.equals(reserva.isCheckedIn())) {
            throw new IllegalStateException("Check-in já foi realizado para esta reserva.");
        }

        OffsetDateTime agora = OffsetDateTime.now();
        // Permite check-in até 5 minutos antes do início
        OffsetDateTime limiteAntes = reserva.getInicio().minusMinutes(5);
        
        if (agora.isBefore(limiteAntes) || agora.isAfter(reserva.getFim())) {
            throw new IllegalStateException("Check-in só pode ser realizado próximo ao horário da reserva.");
        }

        // Validar permissões do usuário
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuarioLogado = usuarioService.findById(userAuthenticated.id());
        List<String> authorities = userAuthenticated.userDetails().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        // MOTORISTA só pode fazer check-in em sua própria reserva
        if (authorities.contains(PermissaoEnum.MOTORISTA.getRole())) {
            Motorista motorista = motoristaService.findByUsuarioId(usuarioLogado.getId());
            if (!reserva.getMotorista().getId().equals(motorista.getId())) {
                throw new IllegalArgumentException("Motorista só pode fazer check-in em suas próprias reservas.");
            }
        }

        // EMPRESA só pode fazer check-in em reservas de seus veículos
        if (authorities.contains(PermissaoEnum.EMPRESA.getRole())) {
            if (!reserva.getCriadoPor().getId().equals(usuarioLogado.getId())) {
                throw new IllegalArgumentException("Empresa só pode fazer check-in em reservas criadas por ela.");
            }
        }


        reserva.setCheckedIn(true);
        reserva.setStatus(StatusReservaEnum.ATIVA);
        reserva.setCheckInEm(agora);
        return reservaRepository.save(reserva);
    }


    public Reserva atualizarReserva (Reserva reserva, UUID usuarioId, ReservaPATCHRequestDTO reservaRequestDTO) {
        final Integer TEMPO_LIMITE_ALTERACAO = 60;
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuarioLogado = usuarioService.findById(userAuthenticated.id());
        Usuario usuarioReserva = usuarioService.findById(usuarioId);
        OffsetDateTime agora = OffsetDateTime.now(DateUtils.FUSO_BRASIL);
        Integer deltaTempo = (int) agora.toInstant().until(reserva.getInicio().toInstant(), ChronoUnit.MINUTES);
      
        if (!reserva.getStatus().equals(StatusReservaEnum.RESERVADA) && !reserva.getStatus().equals(StatusReservaEnum.ATIVA)) throw new IllegalArgumentException("Reserva com status '" + reserva.getStatus() + "' não pode mais ser atualizada.");
        
        if (!reserva.getCriadoPor().equals(usuarioReserva) || !reserva.getMotorista().getUsuario().equals(usuarioReserva) ) throw new EntityNotFoundException("Reserva não encontrada, verifique a reservaId e o usuarioId informados.");
        
        if (deltaTempo < TEMPO_LIMITE_ALTERACAO || deltaTempo < 0){
            throw new IllegalArgumentException("Impossível alterar reserva pois só faltam " + deltaTempo + " minutos para o início e o tempo limite de alteração é de " + TEMPO_LIMITE_ALTERACAO + " minutos.");
        }
        
        if (!usuarioLogado.getId().equals(reserva.getCriadoPor().getId())) reserva.setCriadoPor(usuarioLogado);
        
        if (reservaRequestDTO.getVeiculoId() != null) reserva.setVeiculo(veiculoService.findById(reservaRequestDTO.getVeiculoId()));
        
        if (reservaRequestDTO.getCidadeOrigem() != null) reserva.setCidadeOrigem(reservaRequestDTO.getCidadeOrigem());
       
        if (reservaRequestDTO.getInicio() != null) reserva.setInicio(reservaRequestDTO.getInicio());

        if (reservaRequestDTO.getFim() != null) reserva.setFim(reservaRequestDTO.getFim());

        checarExcecoesReserva(reserva, usuarioLogado, reserva.getMotorista(), reserva.getVeiculo(), ReservaUtils.METODO_PATCH);
        Reserva reservaSalva = reservaRepository.save(reserva);
        try {
            reservaSchedulerService.cancelarSchedulerFinalizaReserva(reservaSalva.getId());
            reservaSchedulerService.cancelarSchedulerNoShowReserva(reservaSalva.getId());
            notificacaoSchedulerService.cancelarSchedulerCheckIn(usuarioId, reservaSalva.getId());
            notificacaoSchedulerService.cancelarSchedulerFimProximo(usuarioId, reservaSalva.getId());
            reservaSchedulerService.agendarFinalizacaoReserva(reservaSalva.toReservaDTO());
            reservaSchedulerService.agendarFinalizacaoNoShow(reservaSalva.toReservaDTO());
            notificacaoSchedulerService.agendarNotificacaoCheckInDisponivel(reservaSalva.getMotorista().getUsuario().getId(), reservaSalva.getId(),reservaSalva.getInicio());
            notificacaoSchedulerService.agendarNotificacaoFimProximo(reservaSalva.getMotorista().getUsuario().getId(), reservaSalva.getId(), reservaSalva.getFim());
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar finalização da reserva: " + e.getMessage());
        }
        return reservaSalva;
    }

    public Reserva realizarCheckout(UUID reservaId) {
        Reserva reserva = findById(reservaId);
        if (reserva.getStatus() != StatusReservaEnum.ATIVA) throw new IllegalArgumentException("Reserva com status '" + reserva.getStatus() + "' não pode ser finalizada.");
        reserva.setStatus(StatusReservaEnum.CONCLUIDA);
        reserva.setCheckOutEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
        Reserva reservaSalva = reservaRepository.save(reserva);
        try {
            reservaSchedulerService.cancelarSchedulerFinalizaReserva(reservaSalva.getId());
            notificacaoSchedulerService.cancelarSchedulerFimProximo(reserva.getMotorista().getUsuario().getId(), reservaId);
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao cancelar schecduler finalização da reserva: " + e.getMessage());
        }
        return reservaSalva;
    }

    public void cancelarReserva(UUID reservaId, UUID usuarioId) {
        Reserva reserva = findById(reservaId);
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> authorities = userAuthenticated.userDetails().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        Usuario usuario = usuarioService.findById(usuarioId);

        if (!StatusReservaEnum.RESERVADA.equals(reserva.getStatus())) {
            throw new IllegalStateException("Só é possivel cancelar uma reserva com status 'RESERVADA'.");
        }
        if(!reserva.getCriadoPor().getId().equals(usuario.getId()) || !reserva.getMotorista().getUsuario().getId().equals(usuario.getId())) {
            if(!authorities.contains(PermissaoEnum.ADMIN.getRole()) && !authorities.contains(PermissaoEnum.GESTOR.getRole())) {
                throw new IllegalArgumentException("Usuário não tem permissão para cancelar esta reserva.");
            }
        }

        reserva.setStatus(StatusReservaEnum.CANCELADA);
        Reserva reservaSalva = reservaRepository.save(reserva);

        try {
            reservaSchedulerService.cancelarSchedulerFinalizaReserva(reservaSalva.getId());
            reservaSchedulerService.cancelarSchedulerNoShowReserva(reservaSalva.getId());
            notificacaoSchedulerService.cancelarSchedulerCheckIn(usuarioId, reservaSalva.getId());
            notificacaoSchedulerService.cancelarSchedulerFimProximo(usuarioId, reservaSalva.getId());
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar finalização da reserva: " + e.getMessage());
        }

    }

    @Transactional
    public void processarNoShow(UUID reservaId){
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        if(!reserva.isPresent() || !reserva.get().getStatus().equals(StatusReservaEnum.RESERVADA)) return;
        reserva.get().setStatus(StatusReservaEnum.REMOVIDA);
        Reserva reservaSalva = reservaRepository.save(reserva.get());
        notificacaoService.notificarNoShow(reserva.get().getMotorista().getUsuario().getId(), reserva.get().getInicio());
        try {
            reservaSchedulerService.cancelarSchedulerFinalizaReserva(reservaSalva.getId());
            reservaSchedulerService.cancelarSchedulerNoShowReserva(reservaSalva.getId());
            notificacaoSchedulerService.cancelarSchedulerCheckIn(reservaSalva.getMotorista().getUsuario().getId(), reservaSalva.getId());
            notificacaoSchedulerService.cancelarSchedulerFimProximo(reservaSalva.getMotorista().getUsuario().getId(), reservaSalva.getId());
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar finalização da reserva: " + e.getMessage());
        }
    }


    public void finalizarReserva (UUID reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        Optional<ReservaRapida> reservaRapida = reservaRapidaService.findById(reservaId);
        if(reserva.isPresent()) {
            if(reserva.get().getStatus() != StatusReservaEnum.CONCLUIDA) {
                reserva.get().setStatus(StatusReservaEnum.CONCLUIDA);
                reserva.get().setCheckOutEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
                reservaRepository.save(reserva.get());
            }
        }
        else if (reservaRapida.isPresent()) {
            if(reservaRapida.get().getStatus() != StatusReservaEnum.CONCLUIDA) {
                reservaRapida.get().setStatus(StatusReservaEnum.CONCLUIDA);
                reservaRapidaService.save(reservaRapida.get());
            }
        } else {
            throw new EntityNotFoundException("Reserva não encontrada.");
        }
    }
}