package com.cptrans.petrocarga.modules.denuncia.service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.denuncia.dto.mapper.DenunciaMapper;
import com.cptrans.petrocarga.modules.denuncia.dto.request.DenunciaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.request.DenunciaRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.request.FinalizarDenunciaRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.dto.response.DenunciaResponseDTO;
import com.cptrans.petrocarga.modules.denuncia.entity.Denuncia;
import com.cptrans.petrocarga.modules.denuncia.repository.DenunciaRepository;
import com.cptrans.petrocarga.modules.denuncia.specification.DenunciaSpecification;
import com.cptrans.petrocarga.modules.denuncia.utils.DenunciaUtils;
import com.cptrans.petrocarga.modules.notificacao.service.NotificacaoService;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.reserva.service.ReservaService;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DenunciaService {
    private final DenunciaRepository denunciaRepository;
    private final NotificacaoService notificacaoService;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;
    private final DenunciaMapper denunciaMapper;

    private final Sort SORT_ASC = Sort.by("criadoEm").ascending();
    private final Sort SORT_DESC = Sort.by("criadoEm").descending();

    @Transactional
    public Denuncia create(UserAuthenticated userAuthenticated, DenunciaRequestDTO request){
        if (denunciaRepository.existsByReservaId(request.getReservaId())) throw new DataIntegrityViolationException ("Já existe uma denuncia criada para essa reserva.");
            
        Usuario usuarioLogado = usuarioService.findByIdAndAtivoTrue(userAuthenticated.id());
        Reserva reserva = reservaService.findByIdAndStatusIn(request.getReservaId(), List.of(StatusReservaEnum.RESERVADA, StatusReservaEnum.ATIVA));
        
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

    public List<Denuncia> findAll() {
        return denunciaRepository.findAll();
    }

    public PageResponseDTO findAllWithFilters(DenunciaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem != OrdemEnum.DESC ? SORT_ASC : SORT_DESC);
        Page<Denuncia> page = denunciaRepository.findAll(DenunciaSpecification.filtrar(filtros), pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        Page<DenunciaResponseDTO> pageResponse = page.map(denunciaMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }

    public Denuncia findById(UUID denunciaId) {
        return denunciaRepository.findById(denunciaId).orElseThrow(() -> new EntityNotFoundException("Denuncia não encontrada."));
    }

    public Denuncia findByIdAutenticado(UserAuthenticated userAuthenticated, UUID denunciaId) {
        List<String> authorities = userAuthenticated.userDetails().getAuthorities().stream().map(GrantedAuthority::toString).toList();

        Denuncia denuncia = denunciaRepository.findById(denunciaId).orElseThrow(() -> new EntityNotFoundException("Denuncia não encontrada."));

        if(!denuncia.getCriadoPor().getId().equals(userAuthenticated.id()) && !authorities.contains(PermissaoEnum.ADMIN.getRole()) && !authorities.contains(PermissaoEnum.GESTOR.getRole())) {
            throw new EntityNotFoundException("Denuncia nao encontrada.");
        }

        return denuncia;
    }
    
    public PageResponseDTO findAllByUsuarioIdAndStatusIn(UUID usuarioId, List<StatusDenunciaEnum> listaStatus, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem != OrdemEnum.DESC ? SORT_ASC : SORT_DESC);
        if (listaStatus == null || listaStatus.isEmpty()){
            Page<Denuncia> page = denunciaRepository.findByCriadoPorId(usuarioId, pageable);
            if (page == null || page.isEmpty()) return new PageResponseDTO(page);
            Page<DenunciaResponseDTO> pageResponse = page.map(denunciaMapper::toResponse);
            return new PageResponseDTO(pageResponse);
        }
        Page<Denuncia> page = denunciaRepository.findByCriadoPorIdAndStatusIn(usuarioId, listaStatus, pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        Page<DenunciaResponseDTO> pageResponse = page.map(denunciaMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }

    public List<Denuncia> findAllByUsuarioId(UUID usuarioId) {
        return denunciaRepository.findByCriadoPorId(usuarioId);
    }

    @Transactional
    public Denuncia iniciarAnalise(Usuario usuarioLogado, UUID denunciaId) {
        Denuncia denuncia = findById(denunciaId);

        if(denuncia.getStatus().equals(StatusDenunciaEnum.EM_ANALISE)) return denuncia;
        if(!denuncia.getStatus().equals(StatusDenunciaEnum.ABERTA)) throw new DataIntegrityViolationException("Denúncia já está em análise ou já foi finalizada.");
        
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
    public Denuncia finalizarAnalise(Usuario usuarioLogado, UUID denunciaId, FinalizarDenunciaRequestDTO respostaRequest) {
        if (respostaRequest.getStatus().equals(StatusDenunciaEnum.ABERTA) || respostaRequest.getStatus().equals(StatusDenunciaEnum.EM_ANALISE)) throw new DataIntegrityViolationException("Status inválido para finalização da denuncia.");
        
        Denuncia denuncia = findById(denunciaId);
        if(!denuncia.getStatus().equals(StatusDenunciaEnum.ABERTA) && !denuncia.getStatus().equals(StatusDenunciaEnum.EM_ANALISE)) throw new DataIntegrityViolationException("Denúncia já foi finalizada.");
        OffsetDateTime agora = DateUtils.agora();

        denuncia.setAtualizadoPor(usuarioLogado);
        denuncia.setAtualizadoEm(agora);
        denuncia.setEncerradoEm(agora);
        denuncia.setStatus(respostaRequest.getStatus());
        denuncia.setResposta(respostaRequest.getResposta());

        Denuncia denunciaAtualizada =  denunciaRepository.save(denuncia);

        Map<String, Object> dadosAdicionais = new HashMap<>();
        dadosAdicionais.put("denunciaId", denunciaAtualizada.getId());

        notificacaoService.notificarDenunciaAtualizada(dadosAdicionais, denunciaAtualizada.getStatus(), denunciaAtualizada.getCriadoPor().getId());
        
        return denunciaAtualizada;
    }
}