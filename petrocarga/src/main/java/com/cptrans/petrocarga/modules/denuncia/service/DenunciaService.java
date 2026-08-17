package com.cptrans.petrocarga.modules.denuncia.service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.auth.utils.AuthUtils;
import com.cptrans.petrocarga.modules.denuncia.dto.mapper.DenunciaMapper;
import com.cptrans.petrocarga.modules.denuncia.dto.request.DenunciaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.request.DenunciaRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.request.FinalizarDenunciaRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.response.DenunciaResponseDTO;
import com.cptrans.petrocarga.modules.denuncia.entity.Denuncia;
import com.cptrans.petrocarga.modules.denuncia.exceptions.DenunciaExceptions;
import com.cptrans.petrocarga.modules.denuncia.repository.DenunciaRepository;
import com.cptrans.petrocarga.modules.denuncia.specification.DenunciaSpecification;
import com.cptrans.petrocarga.modules.denuncia.utils.DenunciaUtils;
import com.cptrans.petrocarga.modules.notificacao.service.NotificacaoService;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.reserva.service.ReservaService;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DenunciaService {
    private final DenunciaRepository denunciaRepository;
    private final NotificacaoService notificacaoService;
    private final ReservaService reservaService;
    private final DenunciaMapper denunciaMapper;

    private final Sort SORT_ASC = Sort.by("criadoEm").ascending();
    private final Sort SORT_DESC = Sort.by("criadoEm").descending();

    @Transactional
    public Denuncia create(UserAuthenticated userAuthenticated, DenunciaRequestDTO request){
        if (denunciaRepository.existsByReservaId(request.getReservaId())) throw new DenunciaExceptions.DenunciaAlreadyExistsException();
            
        Reserva reserva = reservaService.findByIdAndStatusIn(request.getReservaId(), List.of(StatusReservaEnum.RESERVADA, StatusReservaEnum.ATIVA));
        Usuario usuarioLogado = null;
        
        if (reserva.getCriadoPor().getId().equals(userAuthenticated.id())) usuarioLogado = reserva.getCriadoPor();
        if (reserva.getMotorista().getId().equals(userAuthenticated.id())) usuarioLogado = reserva.getMotorista().getUsuario();
        if (usuarioLogado == null ) throw new AuthExceptions.UsuarioNaoAutorizadoException();
        
        DenunciaUtils.validarCriacaoDenuncia(reserva.getStatus(), reserva.getCriadoPor().getId(), reserva.getMotorista().getId(), usuarioLogado.getId());
        
        Denuncia novaDenuncia = new Denuncia(
            request.getDescricao(),
            usuarioLogado,
            reserva,
            request.getTipo()
        );

        Denuncia denunciaSalva = denunciaRepository.save(novaDenuncia);
        
        Map<String, Object> dadosAdicionais = new HashMap<>();
        dadosAdicionais.put("denunciaId", denunciaSalva.getId());
        
        notificacaoService.notificarDenunciaCriada(dadosAdicionais);
        
        return denunciaSalva;
    }

    public PageResponseDTO findAllWithFilters(DenunciaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem != OrdemEnum.DESC ? SORT_ASC : SORT_DESC);
        Page<Denuncia> page = denunciaRepository.findAll(DenunciaSpecification.filtrar(filtros), pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        Page<DenunciaResponseDTO> pageResponse = page.map(denunciaMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }

    public Denuncia findById(UUID denunciaId) {
        return denunciaRepository.findById(denunciaId).orElseThrow(() -> new DenunciaExceptions.DenunciaNotFoundException());
    }

    public Denuncia findByIdAndStatusIn(UUID denunciaId, List<StatusDenunciaEnum> listaStatus) {
        return denunciaRepository.findByIdAndStatusIn(denunciaId, listaStatus).orElseThrow(() -> new DenunciaExceptions.DenunciaNotFoundException());
    }

    public Denuncia findByIdAutenticado(UserAuthenticated userAuthenticated, UUID denunciaId) {
        Denuncia denuncia = findById(denunciaId);

        if (
            !AuthUtils.containsUserId(List.of(denuncia.getCriadoPor().getId())) &&
            !AuthUtils.containsAuthority(List.of(PermissaoEnum.ADMIN.getRole(), PermissaoEnum.GESTOR.getRole()))
        ) throw new AuthExceptions.UsuarioNaoAutorizadoException();

        return denuncia;
    }
    

    public PageResponseDTO findAllByUsuarioIdAndOptionalStatusIn(UUID usuarioId, List<StatusDenunciaEnum> listaStatus, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem != OrdemEnum.DESC ? SORT_ASC : SORT_DESC);
        Page<Denuncia> page;

        if (listaStatus == null || listaStatus.isEmpty()) page = denunciaRepository.findByCriadoPorId(usuarioId, pageable);
        else page = denunciaRepository.findByCriadoPorIdAndStatusIn(usuarioId, listaStatus, pageable);

        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        Page<DenunciaResponseDTO> pageResponse = page.map(denunciaMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }

    public List<Denuncia> findAllByUsuarioId(UUID usuarioId) {
        return denunciaRepository.findByCriadoPorId(usuarioId);
    }

    @Transactional
    public Denuncia iniciarAnalise(Usuario usuarioLogado, UUID denunciaId) {
        Denuncia denuncia = findByIdAndStatusIn(denunciaId, List.of(StatusDenunciaEnum.ABERTA));

        denuncia.setAtualizadoEm(DateUtils.agora());
        denuncia.setStatus(StatusDenunciaEnum.EM_ANALISE);
        denuncia.setAtualizadoPor(usuarioLogado);

        Denuncia denunciaAtualizada =  denunciaRepository.save(denuncia);

        Map<String, Object> dadosAdicionais = new HashMap<>();
        dadosAdicionais.put("denunciaId", denunciaAtualizada.getId());

        notificacaoService.notificarDenunciaAtualizada(dadosAdicionais, denunciaAtualizada.getStatus(), denunciaAtualizada.getCriadoPor().getId());
        
        return denunciaAtualizada;
    }

    @Transactional
    public Denuncia finalizarAnalise(Usuario usuarioLogado, UUID denunciaId, FinalizarDenunciaRequestDTO request) {
        if (!request.getStatus().equals(StatusDenunciaEnum.PROCEDENTE) && !request.getStatus().equals(StatusDenunciaEnum.IMPROCEDENTE)) throw new DenunciaExceptions.DenunciaStatusInvalidException();
        
        Denuncia denuncia = findByIdAndStatusIn(denunciaId, List.of(StatusDenunciaEnum.EM_ANALISE));
        OffsetDateTime agora = DateUtils.agora();

        denuncia.setAtualizadoPor(usuarioLogado);
        denuncia.setAtualizadoEm(agora);
        denuncia.setEncerradoEm(agora);
        denuncia.setStatus(request.getStatus());
        denuncia.setResposta(request.getResposta());

        Denuncia denunciaAtualizada =  denunciaRepository.save(denuncia);

        Map<String, Object> dadosAdicionais = new HashMap<>();
        dadosAdicionais.put("denunciaId", denunciaAtualizada.getId());

        notificacaoService.notificarDenunciaAtualizada(dadosAdicionais, denunciaAtualizada.getStatus(), denunciaAtualizada.getCriadoPor().getId());
        
        return denunciaAtualizada;
    }
}