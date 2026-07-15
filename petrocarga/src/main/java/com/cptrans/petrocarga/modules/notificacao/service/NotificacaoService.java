package com.cptrans.petrocarga.modules.notificacao.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.TipoNotificacaoEnum;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.auth.utils.AuthUtils;
import com.cptrans.petrocarga.modules.events.NotificacaoCriadaEvent;
import com.cptrans.petrocarga.modules.events.SpringDomainEventPublisher;
import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;
import com.cptrans.petrocarga.modules.notificacao.exceptions.NotificacaoExceptions;
import com.cptrans.petrocarga.modules.notificacao.repository.NotificacaoRepository;
import com.cptrans.petrocarga.modules.notificacao.utils.NotificacaoUtils;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.exceptions.UsuarioExceptions;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.transaction.Transactional;

@Service
public class NotificacaoService {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private NotificacaoRepository notificacaoRepository;
    @Autowired
    private SpringDomainEventPublisher eventPublisher;

    private Notificacao createNotificacao(UUID usuarioId, String titulo, String mensagem, TipoNotificacaoEnum tipo, Map<String, Object> dadosAdicionais) {
        UserAuthenticated usuarioLogado = AuthUtils.getUsuarioAutenticado();
        List<String> roles = AuthUtils.getRoles(usuarioLogado);
        Usuario usuarioDestinatario = usuarioService.findByIdAndAtivoTrue(usuarioId);
        NotificacaoUtils.validateByRoles(roles, usuarioDestinatario.getPermissao());
        if (dadosAdicionais == null) dadosAdicionais = new HashMap<>();
        dadosAdicionais.put("remetenteId", usuarioLogado.id());
        dadosAdicionais.put("remetente", usuarioLogado.nome());
        Notificacao novaNotificacao = new Notificacao(usuarioDestinatario.getId(), titulo, mensagem, tipo, dadosAdicionais);
        
        return notificacaoRepository.save(novaNotificacao);
    }

    @Transactional
    private Notificacao saveNotificacao(Notificacao novaNotificacao) {
        return notificacaoRepository.save(novaNotificacao);
    }

    public Page<Notificacao> findAllbyUsuarioId(UUID usuarioId, int numeroPagina, int tamanhoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by("criadaEm").descending());
        Page<Notificacao> page = notificacaoRepository.findByUsuarioId(usuarioId, pageable);
        return page;
    }

    public Page<Notificacao> findAllbyUsuarioIdAndLida(UUID usuarioId, boolean lida, int numeroPagina, int tamanhoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by("criadaEm").descending());
        Page<Notificacao> page = notificacaoRepository.findByUsuarioIdAndLida(usuarioId, lida, pageable);
        return page;
    }

    public Notificacao findById(UUID notificacaoId) {
        return notificacaoRepository.findById(notificacaoId).orElseThrow(() -> new NotificacaoExceptions.NotificacaoNaoEncontradaException());
    }
    public Notificacao findByIdAndUsuarioId(UUID notificacaoId, UUID usuarioId) {
        return notificacaoRepository.findByIdAndUsuarioId(notificacaoId, usuarioId).orElseThrow(() -> new NotificacaoExceptions.NotificacaoNaoEncontradaException());
    }

    public Notificacao findByIdAndSetLida(UUID notificacaoId) {
        UserAuthenticated userAuthenticated = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> authorities = userAuthenticated.userDetails().getAuthorities().stream().map(GrantedAuthority::toString).toList();
        Notificacao notificacao = findById(notificacaoId);
        if ((userAuthenticated.id().equals(notificacao.getUsuarioId()) || (authorities.contains(PermissaoEnum.ADMIN.getRole()) || authorities.contains(PermissaoEnum.GESTOR.getRole())))) {
            notificacao.marcarComoLida();
            return notificacaoRepository.save(notificacao);
        } else {
            throw new AuthExceptions.UsuarioNaoAutorizadoException();
        }
    }

    @Transactional
    public Notificacao sendNotificationToUsuario(UUID usuarioId, Notificacao novaNotificacao) {
        Usuario usuario = usuarioService.findByIdAndAtivo(usuarioId, true);
        novaNotificacao.setUsuarioId(usuario.getId());
        Notificacao notificacaoSalva = createNotificacao(novaNotificacao.getUsuarioId(), novaNotificacao.getTitulo(), novaNotificacao.getMensagem(), novaNotificacao.getTipo(), novaNotificacao.getMetadata());
        eventPublisher.publish(new NotificacaoCriadaEvent(notificacaoSalva));
        return notificacaoSalva;
    }

    @Transactional
    public Notificacao sendNotificationToUsuarioBySystem(UUID usuarioId, Notificacao novaNotificacao, Map<String,Object> dadosAdicionais) {
        Usuario usuario = usuarioService.findByIdAndAtivo(usuarioId, true);
        novaNotificacao.setUsuarioId(usuario.getId());
        
        if (dadosAdicionais != null){
            novaNotificacao.getMetadata().putAll(dadosAdicionais);
        } else novaNotificacao.setMetadata(new HashMap<String,Object>());

        Notificacao notificacaoSalva = saveNotificacao(novaNotificacao);
        eventPublisher.publish(new NotificacaoCriadaEvent(notificacaoSalva));
        return notificacaoSalva;
    }

    @Transactional
    public List<Notificacao> sendNotificacaoToUsuariosByPermissao(PermissaoEnum permissao, Notificacao novaNotificacao) {
        List<Usuario> usuarios = findUsuariosAtivosByPermissao(permissao);
        List<Notificacao> notificacoesSalvas = new ArrayList<>();
        Map<String, Object> dadosAdicionais = new HashMap<>();
        UserAuthenticated userAuthenticated = AuthUtils.getUsuarioAutenticado();

        if (novaNotificacao.getMetadata() != null) dadosAdicionais.putAll(novaNotificacao.getMetadata());
        
        dadosAdicionais.put("remetente", userAuthenticated.nome());
        dadosAdicionais.put("remetenteId", userAuthenticated.id());

        for (Usuario usuario : usuarios) {
            Notificacao novaNotificacaoUsuario = new Notificacao(usuario.getId(), novaNotificacao.getTitulo(), novaNotificacao.getMensagem(), novaNotificacao.getTipo(), dadosAdicionais);
            notificacoesSalvas.add(novaNotificacaoUsuario);
        }
        if (notificacoesSalvas.isEmpty()) return notificacoesSalvas;

        List<Notificacao> notificacoesCriadas = notificacaoRepository.saveAll(notificacoesSalvas);
        if (!notificacoesCriadas.isEmpty()) {
            for (Notificacao notificacao : notificacoesCriadas) {
                eventPublisher.publish(new NotificacaoCriadaEvent(notificacao));
            }
        }
        return notificacoesCriadas;
    }

    @Transactional
    public List<Notificacao> sendNotificacaoToUsuariosByPermissaoBySystem(PermissaoEnum permissao, Notificacao novaNotificacao) {
        List<Usuario> usuarios = findUsuariosAtivosByPermissao(permissao);
        List<Notificacao> notificacoesSalvas = new ArrayList<>();
        Map<String, Object> dadosAdicionais = new HashMap<>();

        if (novaNotificacao.getMetadata() != null) dadosAdicionais.putAll(novaNotificacao.getMetadata());
    
        for (Usuario usuario : usuarios) {
            Notificacao novaNotificacaoUsuario = new Notificacao(usuario.getId(), novaNotificacao.getTitulo(), novaNotificacao.getMensagem(), novaNotificacao.getTipo(), dadosAdicionais);
            notificacoesSalvas.add(novaNotificacaoUsuario);
        }
        if (notificacoesSalvas.isEmpty()) return notificacoesSalvas;

        List<Notificacao> notificacoesCriadas = notificacaoRepository.saveAll(notificacoesSalvas);
        if(!notificacoesCriadas.isEmpty()) {
            for (Notificacao notificacao : notificacoesCriadas) {
                eventPublisher.publish(new NotificacaoCriadaEvent(notificacao));
            }
        }
        return notificacoesCriadas;
    }

    public Notificacao marcarComoLida(UUID usuarioId, UUID notificacaoId) {
        Notificacao notificacao = findByIdAndUsuarioId(notificacaoId, usuarioId);
        notificacao.marcarComoLida();
        return notificacaoRepository.save(notificacao);
    }

    public List<Notificacao> marcarSelecionadasComoLida(UUID usuarioId, List<UUID> listaNotificacaoId) {
        List<Notificacao> notificacoes = findNotificacoesByIdListAndUsuarioId(listaNotificacaoId, usuarioId);
        List<Notificacao> notificacoesLidas = new ArrayList<>();
        for(Notificacao notificacao : notificacoes) {
            notificacao.marcarComoLida();
            notificacoesLidas.add(notificacao);
        }
        return notificacaoRepository.saveAll(notificacoesLidas);
    }

    public void deletarSelecionadas(UUID usuarioId, List<UUID> listaNotificacaoId) {
        List<Notificacao> notificacoes = findNotificacoesByIdListAndUsuarioId(listaNotificacaoId, usuarioId);
        notificacaoRepository.deleteAll(notificacoes);
    }

    public void deleteById(UUID notificacaoId, UUID usuarioId) {
        Notificacao notificacao = findByIdAndUsuarioId(notificacaoId, usuarioId);
        notificacaoRepository.delete(notificacao);
    }

    public void notificarDenunciaCriada(Map<String, Object> dadosAdicionais) {
        Notificacao notificacaoDenuncia = new Notificacao("Nova Denúncia", "Uma nova denúncia foi criada", TipoNotificacaoEnum.DENUNCIA, dadosAdicionais);
        sendNotificacaoToUsuariosByPermissaoBySystem(PermissaoEnum.GESTOR, notificacaoDenuncia);
        sendNotificacaoToUsuariosByPermissaoBySystem(PermissaoEnum.AGENTE, notificacaoDenuncia);
    }

    public void notificarDenunciaAtualizada(Map<String, Object> dadosAdicionais, StatusDenunciaEnum statusDenuncia, UUID usuarioIdDenuncia) {
        Notificacao notificacaoDenuncia = new Notificacao("Denúncia Atualizada", "Sua denúncia foi atualizada para o status: '" + statusDenuncia + "'", TipoNotificacaoEnum.DENUNCIA, dadosAdicionais);
        sendNotificationToUsuarioBySystem(usuarioIdDenuncia, notificacaoDenuncia, dadosAdicionais);
    }

    @Transactional
    public Notificacao notificarOcorrido(UUID usuarioId, String titulo, String mensagem, TipoNotificacaoEnum tipo, String descricaoData, OffsetDateTime dataOcorrido) {
        Map<String, Object> dadosAdicionais = new HashMap<>();
        dadosAdicionais.put(descricaoData, DateUtils.fusoHorarioBrasilia(dataOcorrido).toString());

        Notificacao notificacaoSalva = saveNotificacao(new Notificacao(usuarioId, titulo, mensagem, tipo, dadosAdicionais));

        eventPublisher.publish(new NotificacaoCriadaEvent(notificacaoSalva));

        return notificacaoSalva;
    }

    @Transactional
    public Notificacao notificarCheckInDisponivel(UUID usuarioId, OffsetDateTime inicioReserva) {
        final String TITULO = "Check-in Disponível";
        final String MENSAGEM = "O horário de início da reserva está próximo. Abra o app para confirmar o check-in e não perder sua vaga.";
        final String DESCRICAO_DATA = "inicioReserva";
        return notificarOcorrido(usuarioId, TITULO, MENSAGEM, TipoNotificacaoEnum.RESERVA, DESCRICAO_DATA, inicioReserva);
    }

    @Transactional
    public Notificacao notificarFimProximo(UUID usuarioId, OffsetDateTime fimReserva) {
        final String TITULO = "Fim da Reserva Próximo";
        final String MENSAGEM = "Sua reserva está próxima do fim, realize suas atividades à tempo para evitar problemas.";
        final String DESCRICAO_DATA = "fimReserva";
        return notificarOcorrido(usuarioId, TITULO, MENSAGEM, TipoNotificacaoEnum.RESERVA, DESCRICAO_DATA, fimReserva);
    }

    @Transactional
    public Notificacao notificarNoShow(UUID usuarioId, OffsetDateTime inicioReserva) {
        final String TITULO = "Não Comparecimento à Reserva";
        final String MENSAGEM = "Você não realizou check-in para a sua reserva à tempo. Sua reserva foi removida.";
        final String DESCRICAO_DATA = "inicioReserva";
        return notificarOcorrido(usuarioId, TITULO, MENSAGEM, TipoNotificacaoEnum.RESERVA, DESCRICAO_DATA, inicioReserva) ;
    }

    private List<Usuario> findUsuariosAtivosByPermissao(PermissaoEnum permissao) {
        List<Usuario> usuarios = usuarioService.findByPermissaoAndAtivo(permissao, true);
        if (usuarios == null || usuarios.isEmpty()) {
            throw new UsuarioExceptions.NenhumUsuarioEncontradoByPermissaoException(permissao);
        }
        return usuarios;
    }

    private List<Notificacao> findNotificacoesByIdListAndUsuarioId(List<UUID> notificacaoIdList, UUID usuarioId) { 
        List<Notificacao> notificacoes = notificacaoRepository.findByIdInAndUsuarioId(notificacaoIdList, usuarioId); 
        if (notificacoes == null || notificacoes.isEmpty()) {
            throw new NotificacaoExceptions.NenhumaNotificacaoEncontradaException();
        }
        return notificacoes;
    } 

}