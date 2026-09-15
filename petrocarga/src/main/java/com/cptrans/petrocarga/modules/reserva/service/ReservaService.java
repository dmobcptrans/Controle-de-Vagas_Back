package com.cptrans.petrocarga.modules.reserva.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.enums.TipoNotificacaoEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.auth.utils.AuthUtils;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.service.MotoristaService;
import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;
import com.cptrans.petrocarga.modules.notificacao.service.NotificacaoService;
import com.cptrans.petrocarga.modules.reserva.dto.mapper.ReservaMapper;
import com.cptrans.petrocarga.modules.reserva.dto.request.ReservaPATCHRequestDTO;
import com.cptrans.petrocarga.modules.reserva.dto.request.ReservaRequestDTO;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.reserva.exceptions.ReservaExceptions;
import com.cptrans.petrocarga.modules.reserva.repository.ReservaRepository;
import com.cptrans.petrocarga.modules.reserva.specification.ReservaSpecification;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.reservaRapida.dto.mapper.ReservaRapidaMapper;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.reservaRapida.repository.ReservaRapidaRepository;
import com.cptrans.petrocarga.modules.reservaRapida.service.ReservaRapidaService;
import com.cptrans.petrocarga.modules.reservaRules.ReservaRules;
import com.cptrans.petrocarga.modules.scheduler.notificacao.handler.NotificacaoSchedulerService;
import com.cptrans.petrocarga.modules.scheduler.reserva.handler.ReservaSchedulerService;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.service.VagaService;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculo.service.VeiculoService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final ReservaRapidaRepository reservaRapidaRepository;
    private final MotoristaService motoristaService;
    private final VeiculoService veiculoService;
    private final UsuarioService usuarioService;
    private final UsuarioUtils usuarioUtils;
    private final VagaService vagaService;
    private final ReservaRapidaService reservaRapidaService;
    private final NotificacaoService notificacaoService;
    private final ReservaUtils reservaUtils;
    private final ReservaSchedulerService reservaSchedulerService;
    private final NotificacaoSchedulerService notificacaoSchedulerService;
    private final ReservaMapper reservaMapper;
    private final ReservaRapidaMapper reservaRapidaMapper;
    private final ReservaRules reservaRules;

    public List<ReservaDTO> findAll(List<StatusReservaEnum> status, UUID vagaId, String placa, LocalDate data,  UUID usuarioId, Integer mes, Integer ano) {
        if(status == null) status = new ArrayList<>();

        List<Reserva> reservas = reservaRepository.findAll(ReservaSpecification.filtrar(usuarioId, vagaId, placa, data, mes, ano, status));
        List<ReservaRapida> reservasRapidas = reservaRapidaService.findAllWithFilters(usuarioId, vagaId, placa, data, mes, ano, status);
        List<ReservaDTO> reservasDTO = reservaUtils.juntarReservas(reservas, reservasRapidas);
        return reservasDTO;
    }
    

    public Reserva findById(UUID reservaId) {
        Reserva reserva = reservaRepository.findByIdWithJoins(reservaId);

        if (
            AuthUtils.containsAuthority(List.of(PermissaoEnum.MOTORISTA.getRole())) &&
            !AuthUtils.containsUserId(List.of(reserva.getMotorista().getId()))
        ) throw new AuthExceptions.UsuarioNaoAutorizadoException();

        if (
            AuthUtils.containsAuthority(List.of(PermissaoEnum.EMPRESA.getRole())) &&
            !AuthUtils.containsUserId(List.of(reserva.getCriadoPor().getId(), reserva.getVeiculo().getUsuario().getId()))
        ) throw new AuthExceptions.UsuarioNaoAutorizadoException();

        return reserva;
    }

    public Reserva findByIdAndStatusIn(UUID reservaId, List<StatusReservaEnum> status) {
        if (status == null) status = List.of(StatusReservaEnum.RESERVADA, StatusReservaEnum.ATIVA);
        return reservaRepository.findByIdAndStatusIn(reservaId, status).orElseThrow(() -> new ReservaExceptions.ReservaNotFoundException());
    }

    public Page<Reserva> findByCriadoPorIdOrMotoristaId(UUID usuarioId, List<StatusReservaEnum> status, Integer numeroPagina, Integer tamanhoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by("criadoEm").descending());
        Page<Reserva> reservasPage;
        if (status != null && !status.isEmpty()) reservasPage = reservaRepository.findByStatusInAndCriadoPorIdOrMotoristaId(status, usuarioId, usuarioId, pageable);
        else reservasPage = reservaRepository.findByCriadoPorIdOrMotoristaId(usuarioId, usuarioId, pageable);
        return reservasPage;
    }

    public Reserva createReserva(ReservaRequestDTO request) {
        reservaRules.validacoesTemporais(request.getInicio(), request.getFim());

        Vaga vaga = vagaService.findById(request.getVagaId());
        reservaRules.validacoesDaVaga(vaga, request.getInicio(), request.getFim());
        
        UserAuthenticated userAuthenticated = AuthUtils.getUsuarioAutenticado();
        Usuario usuarioLogado = usuarioService.findByIdAndAtivoTrue(userAuthenticated.id());

        Motorista motorista = motoristaService.findByIdAndAtivoTrue(request.getMotoristaId());
        Veiculo veiculo = veiculoService.findAtivoByIdAndUsuarioId(request.getVeiculoId(), userAuthenticated.id());

        reservaRules.validacoesDeAutorizacao(userAuthenticated.id(), motorista, veiculo);
        
        Reserva novaReserva = reservaMapper.toEntity(request, vaga, motorista, veiculo, usuarioLogado);
        Usuario criadoPor = novaReserva.getCriadoPor();
        String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
        ReservaDTO dto = reservaMapper.toReservaDTO(novaReserva, cpfOrCnpjCriador);
        
        reservaRules.validacoesDeConflito(vaga, dto);

        if (vaga.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)){
            novaReserva.setPosicaoPerpendicular(dto.getPosicaoPerpendicular());
        }

        Reserva reservaSalva = reservaRepository.save(novaReserva);
        agendarSchedulers(reservaMapper.toReservaDTO(reservaSalva, cpfOrCnpjCriador));
        return reservaSalva;
    }

    public void deleteById(UUID id) {
        reservaRepository.deleteById(id);
    }

    public List<ReservaDTO> getReservasByVagaIdAndData(UUID vagaId, LocalDate data, List<StatusReservaEnum> status) {
        List<Reserva> reservasNormais = reservaRepository.findByVagaIdAndStatusIn(
            vagaId,
            status
        );

        List<ReservaRapida> reservasRapidas = reservaRapidaRepository.findByVagaIdAndStatusIn(
            vagaId,
            status
        );
       
        List<ReservaDTO> reservas = reservaUtils.juntarReservas(reservasNormais, reservasRapidas);

        Instant inicioDoDia = data
            .atStartOfDay(DateUtils.FUSO_BRASILIA)
            .toInstant();

        Instant fimDoDia = data
            .plusDays(1)
            .atStartOfDay(DateUtils.FUSO_BRASILIA)
            .toInstant();

        return reservas.stream()
            .filter(reserva -> {
                Instant inicioReserva = reserva.getInicio().toInstant();
                Instant fimReserva = reserva.getFim().toInstant();

                return fimReserva.isAfter(inicioDoDia)
                    && inicioReserva.isBefore(fimDoDia);
            })
            .toList();
    }


    public List<ReservaDTO> getReservasByVagaIdDataAndPlaca(UUID vagaId, LocalDate data, String placa, List<StatusReservaEnum> status) {
        if (placa == null) throw new IllegalArgumentException("Placa não pode ser nula ou vazia.");
        placa = placa.trim().toUpperCase();
        List<Reserva> reservasNormais = reservaRepository.findByVagaIdAndVeiculoPlacaIgnoringCaseAndStatusIn(
            vagaId,
            placa,
            status
        );

        List<ReservaRapida> reservasRapidas = reservaRapidaRepository.findByVagaIdAndPlacaIgnoringCaseAndStatusIn(
            vagaId,
            placa,
            status
        );
       
        List<ReservaDTO> reservas = reservaUtils.juntarReservas(reservasNormais, reservasRapidas);

        Instant inicioDoDia = data
            .atStartOfDay(DateUtils.FUSO_BRASILIA)
            .toInstant();

        Instant fimDoDia = data
            .plusDays(1)
            .atStartOfDay(DateUtils.FUSO_BRASILIA)
            .toInstant();

        return reservas.stream()
            .filter(reserva -> {
                Instant inicioReserva = reserva.getInicio().toInstant();
                Instant fimReserva = reserva.getFim().toInstant();

                return fimReserva.isAfter(inicioDoDia)
                    && inicioReserva.isBefore(fimDoDia);
            })
            .toList();
    }

    public List<ReservaDTO> getReservasAtivasByPlaca(String placa){
        if (placa == null) return List.of();
        placa = placa.trim().toUpperCase();
        List<Reserva> reservasPorPlaca = reservaRepository.findByVeiculoPlacaIgnoringCaseAndStatusIn(placa, new ArrayList<>(List.of(StatusReservaEnum.ATIVA, StatusReservaEnum.RESERVADA)));
        List<ReservaRapida> reservasRapidasPorPlaca = reservaRapidaService.findByPlacaAtiva(placa);
        List<ReservaDTO> listaReservasAtivasPorPlaca = reservaUtils.juntarReservas(reservasPorPlaca, reservasRapidasPorPlaca);
        return listaReservasAtivasPorPlaca;
    }

    public List<Intervalo> getIntervalosBloqueados(
        UUID vagaId,
        LocalDate data,
        TipoVeiculoEnum tipoVeiculo
    ) {
        if (data == null) {
            throw new IllegalArgumentException("A data da reserva é obrigatória.");
        }

        if (tipoVeiculo == null) {
            throw new IllegalArgumentException("O tipo de veículo é obrigatório.");
        }

        Vaga vaga = vagaService.findById(vagaId);

        if (vaga.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)) {
            return getIntervalosBloqueadosPerpendicular(vaga, data, tipoVeiculo);
        }

        return getIntervalosBloqueadosParalela(vaga, data, tipoVeiculo);
    }


    private List<Intervalo> getIntervalosBloqueadosParalela(
        Vaga vaga,
        LocalDate data,
        TipoVeiculoEnum tipoVeiculo
    ) {
        if (vaga.getComprimento() == null || vaga.getComprimento() <= 0) {
            throw new IllegalArgumentException(
                "Vaga do tipo paralela deve ter o campo 'comprimento' preenchido."
            );
        }

        int capacidadeTotal = vaga.getComprimento();
        int comprimentoVeiculoDesejado = tipoVeiculo.getComprimento();

        if (comprimentoVeiculoDesejado > capacidadeTotal) {
            throw new IllegalArgumentException(
                "O veículo selecionado é maior do que o tamanho da vaga."
            );
        }

        List<ReservaDTO> reservas = getReservasByVagaIdAndData(
            vaga.getId(),
            data,
            new ArrayList<>(
                List.of(
                    StatusReservaEnum.RESERVADA,
                    StatusReservaEnum.ATIVA
                )
            )
        );

        if (reservas.isEmpty()) {
            return List.of();
        }


        Instant inicioDoDia = data
            .atStartOfDay(DateUtils.FUSO_BRASILIA)
            .toInstant();

        Instant fimDoDia = data
            .plusDays(1)
            .atStartOfDay(DateUtils.FUSO_BRASILIA)
            .toInstant();

        TreeSet<Instant> pontos = new TreeSet<>();

        reservas.forEach(reserva -> {
            Instant inicioReserva = reserva.getInicio().toInstant();
            Instant fimReserva = reserva.getFim().toInstant();

            pontos.add(
                inicioReserva.isBefore(inicioDoDia)
                    ? inicioDoDia
                    : inicioReserva
            );

            pontos.add(
                fimReserva.isAfter(fimDoDia)
                    ? fimDoDia
                    : fimReserva
            );
        });

        List<Instant> timeline = new ArrayList<>(pontos);

        List<Intervalo> intervalosBloqueados = new ArrayList<>();
        Intervalo atual = null;

        for (int i = 0; i < timeline.size() - 1; i++) {
            Instant inicio = timeline.get(i);
            Instant fim = timeline.get(i + 1);

            if (!inicio.isBefore(fim)) {
                continue;
            }

            int ocupacaoAtual = 0;

            for (ReservaDTO reserva : reservas) {
                Instant inicioReserva = reserva.getInicio().toInstant();
                Instant fimReserva = reserva.getFim().toInstant();

                boolean sobrepoe =
                    inicioReserva.isBefore(fim) &&
                    fimReserva.isAfter(inicio);

                if (sobrepoe) {
                    ocupacaoAtual += reserva.getTamanhoVeiculo();
                }
            }

            int espacoRestante = capacidadeTotal - ocupacaoAtual;

            boolean cabe = espacoRestante >= comprimentoVeiculoDesejado;

            if (!cabe) {
                OffsetDateTime dtoInicio = OffsetDateTime.ofInstant(
                    inicio,
                    DateUtils.FUSO_BRASILIA
                );

                OffsetDateTime dtoFim = OffsetDateTime.ofInstant(
                    fim,
                    DateUtils.FUSO_BRASILIA
                );

                if (atual == null) {
                    atual = new Intervalo(dtoInicio, dtoFim);
                } else {
                    atual.setFim(dtoFim);
                }

            } else if (atual != null) {
                intervalosBloqueados.add(atual);
                atual = null;
            }
        }

        if (atual != null) {
            intervalosBloqueados.add(atual);
        }

        return intervalosBloqueados;
    }


    private List<Intervalo> getIntervalosBloqueadosPerpendicular(
        Vaga vaga,
        LocalDate data,
        TipoVeiculoEnum tipoVeiculo
    ) {
        if (
            vaga.getComprimento() == null ||
            vaga.getComprimento() <= 0 ||
            vaga.getQuantidade() == null ||
            vaga.getQuantidade() <= 0
        ) {
            throw new IllegalArgumentException(
                "Vaga do tipo perpendicular deve ter os campos " +
                "'comprimento' e 'quantidade' preenchidos."
            );
        }

        int comprimentoPorPosicao = vaga.getComprimento();
        int quantidadePosicoes = vaga.getQuantidade();
        int comprimentoVeiculoDesejado = tipoVeiculo.getComprimento();

        if (comprimentoVeiculoDesejado > comprimentoPorPosicao) {
            throw new IllegalArgumentException(
                "O veículo selecionado é maior do que o tamanho permitido " +
                "por posição nesta vaga."
            );
        }

        List<ReservaDTO> reservas = getReservasByVagaIdAndData(
            vaga.getId(),
            data,
            new ArrayList<>(
                List.of(
                    StatusReservaEnum.RESERVADA,
                    StatusReservaEnum.ATIVA
                )
            )
        );

        if (reservas.isEmpty()) {
            return List.of();
        }


        Instant inicioDoDia = data
            .atStartOfDay(DateUtils.FUSO_BRASILIA)
            .toInstant();

        Instant fimDoDia = data
            .plusDays(1)
            .atStartOfDay(DateUtils.FUSO_BRASILIA)
            .toInstant();

        TreeSet<Instant> pontos = new TreeSet<>();

        reservas.forEach(reserva -> {
            Instant inicioReserva = reserva.getInicio().toInstant();
            Instant fimReserva = reserva.getFim().toInstant();

            pontos.add(
                inicioReserva.isBefore(inicioDoDia)
                    ? inicioDoDia
                    : inicioReserva
            );

            pontos.add(
                fimReserva.isAfter(fimDoDia)
                    ? fimDoDia
                    : fimReserva
            );
        });

        List<Instant> timeline = new ArrayList<>(pontos);

        List<Intervalo> intervalosBloqueados = new ArrayList<>();
        Intervalo atual = null;

        for (int i = 0; i < timeline.size() - 1; i++) {
            Instant inicio = timeline.get(i);
            Instant fim = timeline.get(i + 1);

            if (!inicio.isBefore(fim)) {
                continue;
            }

            Map<Integer, Integer> ocupacaoPorPosicao = new HashMap<>();

            for (ReservaDTO reserva : reservas) {
                Instant inicioReserva = reserva.getInicio().toInstant();
                Instant fimReserva = reserva.getFim().toInstant();

                boolean sobrepoe =
                    inicioReserva.isBefore(fim) &&
                    fimReserva.isAfter(inicio);

                if (!sobrepoe) {
                    continue;
                }

                Integer posicao = reserva.getPosicaoPerpendicular();

                if (
                    posicao == null ||
                    posicao <= 0 ||
                    posicao > quantidadePosicoes
                ) {
                    throw new IllegalArgumentException(
                        "Reserva perpendicular possui posição inválida."
                    );
                }

                ocupacaoPorPosicao.merge(
                    posicao,
                    reserva.getTamanhoVeiculo(),
                    Integer::sum
                );
            }

            boolean cabeEmAlgumaPosicao = false;

            for (int posicao = 1; posicao <= quantidadePosicoes; posicao++) {
                int ocupado = ocupacaoPorPosicao.getOrDefault(posicao, 0);

                if (
                    ocupado + comprimentoVeiculoDesejado
                        <= comprimentoPorPosicao
                ) {
                    cabeEmAlgumaPosicao = true;
                    break;
                }
            }

            if (!cabeEmAlgumaPosicao) {
                OffsetDateTime dtoInicio = OffsetDateTime.ofInstant(
                    inicio,
                    DateUtils.FUSO_BRASILIA
                );

                OffsetDateTime dtoFim = OffsetDateTime.ofInstant(
                    fim,
                    DateUtils.FUSO_BRASILIA
                );

                if (atual == null) {
                    atual = new Intervalo(dtoInicio, dtoFim);
                } else {
                    atual.setFim(dtoFim);
                }

            } else if (atual != null) {
                intervalosBloqueados.add(atual);
                atual = null;
            }
        }

        if (atual != null) {
            intervalosBloqueados.add(atual);
        }

        return intervalosBloqueados;
    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Intervalo {
        private OffsetDateTime inicio;
        private OffsetDateTime fim;
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
        UUID usuarioIdLogado = AuthUtils.getIdUsuarioAutenticado();
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        Optional<ReservaRapida> reservaRapida = reservaRapidaService.findById(reservaId);
        ReservaDTO reservaDTO = null;

        if (!reserva.isPresent() && !reservaRapida.isPresent()) throw new ReservaExceptions.ReservaNotFoundException();
       
        if (reserva.isPresent()){
            Reserva reservaEncontrada = reserva.get();
            if (!StatusReservaEnum.RESERVADA.equals(reservaEncontrada.getStatus()) && !StatusReservaEnum.ATIVA.equals(reservaEncontrada.getStatus())) {
                throw new IllegalStateException("Só é possivel finalizar uma reserva com status 'RESERVADA' ou 'ATIVA'.");
            }
            reservaEncontrada.setStatus(StatusReservaEnum.REMOVIDA);
            Reserva reservaSalva = reservaRepository.save(reservaEncontrada);
            Usuario criadoPor = reservaSalva.getCriadoPor();
            String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
            reservaDTO = reservaMapper.toReservaDTO(reservaSalva, cpfOrCnpjCriador);
        }
        if (reservaRapida.isPresent()){
            if (!StatusReservaEnum.RESERVADA.equals(reservaRapida.get().getStatus()) && !StatusReservaEnum.ATIVA.equals(reservaRapida.get().getStatus())) {
                throw new IllegalStateException("Só é possivel finalizar uma reserva com status 'RESERVADA' ou 'ATIVA'.");
            }
            reservaRapida.get().setStatus(StatusReservaEnum.REMOVIDA);
            ReservaRapida reservaRapidaSalva = reservaRapidaService.save(reservaRapida.get());
            Agente criadoPor = reservaRapidaSalva.getAgente();
            reservaDTO = reservaRapidaMapper.toReservaDTO(reservaRapidaSalva, criadoPor.getCpfCripto());
        }
        
        if (!reservaDTO.getCriadoPor().getId().equals(usuarioIdLogado)) {
            Notificacao notificacao = new Notificacao("Checkout Forçado","Sua reserva foi removida por um agente ou gestor. Realize uma nova reserva se necessário", TipoNotificacaoEnum.RESERVA);
            notificacaoService.sendNotificationToUsuarioBySystem(reservaDTO.getCriadoPor().getId(), notificacao, null);
            if (reservaDTO.getMotoristaId() != null && !reservaDTO.getCriadoPor().getId().equals(reservaDTO.getMotoristaId())) {
                notificacaoService.sendNotificationToUsuarioBySystem(reservaDTO.getMotoristaId(), notificacao, null);
            }
        }

        if (reserva.isPresent()) cancelarSchedulers(reserva.get().getCriadoPor().getId(), reservaDTO.getId(), reservaDTO.getMotoristaId());
        
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

        if (!StatusReservaEnum.RESERVADA.equals(reserva.getStatus())) throw new IllegalStateException("Reserva não está ativa.");

        if (reserva.getCheckedIn()) throw new IllegalStateException("Check-in já foi realizado para esta reserva.");

        OffsetDateTime agora = DateUtils.agora();
        OffsetDateTime limiteAntes = reserva.getInicio().minusMinutes(5);
        
        if (agora.toInstant().isBefore(limiteAntes.toInstant()) || agora.toInstant().isAfter(reserva.getFim().toInstant())) {
            throw new IllegalStateException("Check-in só pode ser realizado próximo ao horário da reserva.");
        }

        if (
            AuthUtils.containsAuthority(List.of(PermissaoEnum.MOTORISTA.getRole())) &&
            !AuthUtils.containsUserId(List.of(reserva.getMotorista().getId()))
        ) throw new IllegalArgumentException("Motorista só pode fazer check-in em suas próprias reservas.");

        if (
            AuthUtils.containsAuthority(List.of(PermissaoEnum.EMPRESA.getRole())) &&
            !AuthUtils.containsUserId(List.of(reserva.getCriadoPor().getId()))
        ) throw new IllegalArgumentException("Empresa só pode fazer check-in em reservas criadas por ela.");

        reserva.setCheckedIn(true);
        reserva.setStatus(StatusReservaEnum.ATIVA);
        reserva.setCheckInEm(agora);
        return reservaRepository.save(reserva);
    }


    public Reserva atualizarReserva (UserAuthenticated userAuthenticated, UUID reservaId, UUID usuarioId, ReservaPATCHRequestDTO reservaRequestDTO) {
        final Integer TEMPO_LIMITE_ALTERACAO = 60;
        Reserva reserva = reservaRepository.findByIdAndUsuarioId(reservaId, usuarioId).orElseThrow(() -> new ReservaExceptions.ReservaNotFoundException());
        OffsetDateTime agora = DateUtils.agora();
        Integer deltaTempo = (int) agora.toInstant().until(reserva.getInicio().toInstant(), ChronoUnit.MINUTES);
        
        if (!reserva.getStatus().equals(StatusReservaEnum.RESERVADA) && !reserva.getStatus().equals(StatusReservaEnum.ATIVA)) throw new IllegalArgumentException("Reserva com status '" + reserva.getStatus() + "' não pode mais ser atualizada.");
        
        if (deltaTempo < TEMPO_LIMITE_ALTERACAO || deltaTempo < 0) throw new ReservaExceptions.TempoAlteracaoEsgotadoException(deltaTempo, TEMPO_LIMITE_ALTERACAO);
        
        if (reservaRequestDTO.getMotoristaId() != null && !reservaRequestDTO.getMotoristaId().equals(reserva.getMotorista().getId())) reserva.setMotorista(motoristaService.findByIdAndAtivoTrue(reservaRequestDTO.getMotoristaId()));

        if (reservaRequestDTO.getVeiculoId() != null) reserva.setVeiculo(veiculoService.findAtivoByIdAndUsuarioId(reservaRequestDTO.getVeiculoId(), userAuthenticated.id()));
        
        if (reservaRequestDTO.getCidadeOrigem() != null) reserva.setCidadeOrigem(reservaRequestDTO.getCidadeOrigem());
       
        if (reservaRequestDTO.getInicio() != null) reserva.setInicio(reservaRequestDTO.getInicio());

        if (reservaRequestDTO.getFim() != null) reserva.setFim(reservaRequestDTO.getFim());

        Vaga vaga = reserva.getVaga();

        reservaRules.validacoesTemporais(reserva.getInicio(), reserva.getFim());
        reservaRules.validacoesDaVaga(vaga, reserva.getInicio(), reserva.getFim());
        reservaRules.validacoesDeAutorizacao(usuarioId, reserva.getMotorista(), reserva.getVeiculo());
        
        Usuario criadoPor = reserva.getCriadoPor();
        String cpfOrCnpjCriador = usuarioUtils.getCpfOrCnpjByPermissaoAndId(criadoPor.getPermissao(), criadoPor.getId());
        ReservaDTO dto = reservaMapper.toReservaDTO(reserva, cpfOrCnpjCriador);
        
        reservaRules.validacoesDeConflito(vaga, dto);

        if (vaga.getTipoVaga().equals(TipoVagaEnum.PERPENDICULAR)){
            reserva.setPosicaoPerpendicular(dto.getPosicaoPerpendicular());
        }

        Reserva reservaSalva = reservaRepository.save(reserva);
        agendarSchedulers(reservaMapper.toReservaDTO(reservaSalva, cpfOrCnpjCriador));
        return reservaSalva;
    }

    public Reserva realizarCheckout(UUID reservaId) {
        Reserva reserva = findById(reservaId);
        if (!reserva.getStatus().equals(StatusReservaEnum.ATIVA)) throw new IllegalArgumentException("Reserva com status '" + reserva.getStatus() + "' não pode ser finalizada.");
        reserva.setStatus(StatusReservaEnum.CONCLUIDA);
        reserva.setCheckOutEm(DateUtils.agora());
        Reserva reservaSalva = reservaRepository.save(reserva);
        cancelarSchedulers(reserva.getCriadoPor().getId(), reservaSalva.getId(), reservaSalva.getMotorista().getId());
        return reservaSalva;
    }

    public void cancelarReserva(UUID reservaId, UUID usuarioId) {
        Reserva reserva = findById(reservaId);

        if (!StatusReservaEnum.RESERVADA.equals(reserva.getStatus())) throw new IllegalStateException("Só é possivel cancelar uma reserva com status 'RESERVADA'.");
        
        if (
            !AuthUtils.containsUserId(List.of(reserva.getCriadoPor().getId(), reserva.getMotorista().getId())) &&
            !AuthUtils.containsAuthority(List.of(PermissaoEnum.ADMIN.getRole(), PermissaoEnum.GESTOR.getRole()))
        ) throw new AuthExceptions.UsuarioNaoAutorizadoException();

        reserva.setStatus(StatusReservaEnum.CANCELADA);
        Reserva reservaSalva = reservaRepository.save(reserva);

        cancelarSchedulers(reservaSalva.getCriadoPor().getId(), reservaSalva.getId(), reservaSalva.getMotorista().getId());

    }

    @Transactional
    public void processarNoShow(UUID reservaId){
        Optional<Reserva> reservaOptional = reservaRepository.findById(reservaId);
        if (!reservaOptional.isPresent() || !reservaOptional.get().getStatus().equals(StatusReservaEnum.RESERVADA)) return;
        Reserva reserva = reservaOptional.get();
        reserva.setStatus(StatusReservaEnum.REMOVIDA);
        Reserva reservaSalva = reservaRepository.save(reserva);
        if (!reservaSalva.getCriadoPor().getId().equals(reservaSalva.getMotorista().getId())){
            notificacaoService.notificarNoShow(reservaSalva.getCriadoPor().getId(), reservaSalva.getInicio());
        }
        notificacaoService.notificarNoShow(reservaSalva.getMotorista().getId(), reservaSalva.getInicio());
        cancelarSchedulers(reservaSalva.getCriadoPor().getId(), reservaSalva.getId(), reservaSalva.getMotorista().getId());
    }


    public void finalizarReserva (UUID reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        Optional<ReservaRapida> reservaRapida = reservaRapidaService.findById(reservaId);
        if(reserva.isPresent()) {
            if(reserva.get().getStatus() != StatusReservaEnum.CONCLUIDA) {
                reserva.get().setStatus(StatusReservaEnum.CONCLUIDA);
                reserva.get().setCheckOutEm(DateUtils.agora());
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

    private void cancelarSchedulers(UUID criadoPorId, UUID reservaId, UUID motoristaId) {
        try {
            reservaSchedulerService.cancelarSchedulerFinalizaReserva(reservaId);
            reservaSchedulerService.cancelarSchedulerNoShowReserva(reservaId);
            notificacaoSchedulerService.cancelarSchedulerCheckIn(motoristaId, reservaId);
            notificacaoSchedulerService.cancelarSchedulerFimProximo(motoristaId, reservaId);
            notificacaoSchedulerService.cancelarSchedulerNovaReserva(criadoPorId, reservaId, motoristaId);
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao cancelar schedulers da reserva: " + e.getMessage());
        }
    }

    private void agendarSchedulers(ReservaDTO reservaDTO) {
        UUID reservaId = reservaDTO.getId();
        UUID criadoPorId = reservaDTO.getCriadoPor().getId();
        String criadoPorNome = reservaDTO.getCriadoPor().getNome();
        UUID motoristaId = reservaDTO.getMotoristaId();
        OffsetDateTime inicio = reservaDTO.getInicio();
        OffsetDateTime fim = reservaDTO.getFim();
        OffsetDateTime criadoEm = reservaDTO.getCriadoEm();
        try {
            cancelarSchedulers(criadoPorId, reservaId, motoristaId);
            reservaSchedulerService.agendarFinalizacaoReserva(reservaDTO);
            reservaSchedulerService.agendarFinalizacaoNoShow(reservaDTO);
            if (criadoPorId != null && motoristaId != null && !criadoPorId.equals(motoristaId)) {
                notificacaoSchedulerService.agendarNotificacaoCheckInDisponivel(criadoPorId, reservaId, inicio);
                notificacaoSchedulerService.agendarNotificacaoFimProximo(criadoPorId, reservaId, fim);
                notificacaoSchedulerService.agendarNotificacaoNovaReserva(motoristaId, criadoPorNome, reservaId, motoristaId, criadoEm);
            }
            notificacaoSchedulerService.agendarNotificacaoCheckInDisponivel(motoristaId, reservaId,inicio);
            notificacaoSchedulerService.agendarNotificacaoFimProximo(motoristaId, reservaId, fim);
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar schedulers da reserva: " + e.getMessage());
        }
    }
}