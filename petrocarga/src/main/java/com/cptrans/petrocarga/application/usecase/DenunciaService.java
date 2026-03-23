package com.cptrans.petrocarga.application.usecase;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.FinalizarDenunciaRequestDTO;
import com.cptrans.petrocarga.domain.entities.Denuncia;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.domain.enums.TipoDenunciaEnum;
import com.cptrans.petrocarga.domain.repositories.DenunciaRepository;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.DenunciaUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class DenunciaService {
    
    @Autowired
    private DenunciaRepository denunciaRepository;
    @Autowired
    private NotificacaoService notificacaoService;

    @Transactional
    public Denuncia create(Denuncia novaDenuncia){
        if (denunciaRepository.existsByReservaId(novaDenuncia.getReserva().getId())) throw new DataIntegrityViolationException ("Já existe uma denuncia criada para essa reserva.");
        
        DenunciaUtils.validarCriacaoDenuncia(novaDenuncia);
        
        Denuncia denunciaSalva = denunciaRepository.save(novaDenuncia);
        
        Map<String, Object> dadosAdicionais = new HashMap<>();
        dadosAdicionais.put("denunciaId", denunciaSalva.getId());
        
        notificacaoService.notificarDenunciaCriada(dadosAdicionais);
        
        return denunciaSalva;
    }

    public List<Denuncia> findAll() {
        return denunciaRepository.findAll();
    }

    public List<Denuncia> findAllWithFilters(UUID vagaId, List<StatusDenunciaEnum> listaStatus, List<TipoDenunciaEnum> listaTipos) {
        List<Denuncia> response = new ArrayList<>();

        if (vagaId != null && listaStatus != null && !listaStatus.isEmpty() && listaTipos != null && !listaTipos.isEmpty()) return denunciaRepository.findByVagaIdAndStatusInAndTipoIn(vagaId, listaStatus, listaTipos);

        if (vagaId != null) response.addAll(denunciaRepository.findByVagaId(vagaId));
        
        if (listaStatus != null && !listaStatus.isEmpty()) {
            if(response.isEmpty()) response.addAll(denunciaRepository.findByStatusIn(listaStatus));
            else response = response.stream().filter(denuncia -> listaStatus.contains(denuncia.getStatus())).toList();
        }

        if (listaTipos != null && !listaTipos.isEmpty()) {
            if(response.isEmpty()) response.addAll(denunciaRepository.findByTipoIn(listaTipos));
            else response = response.stream().filter(denuncia -> listaTipos.contains(denuncia.getTipo())).toList();
        }

        return response;
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
    
    public List<Denuncia> findAllByUsuarioIdAndStatus(UUID usuarioId, StatusDenunciaEnum status) {
        return denunciaRepository.findByCriadoPorIdAndStatus(usuarioId, status);
    }

    public List<Denuncia> findAllByUsuarioId(UUID usuarioId) {
        return denunciaRepository.findByCriadoPorId(usuarioId);
    }

    @Transactional
    public Denuncia iniciarAnalise(Usuario usuarioLogado, UUID denunciaId) {
        Denuncia denuncia = findById(denunciaId);

        if(denuncia.getStatus().equals(StatusDenunciaEnum.EM_ANALISE)) return denuncia;
        if(!denuncia.getStatus().equals(StatusDenunciaEnum.ABERTA)) throw new DataIntegrityViolationException("Denúncia já está em análise ou já foi finalizada.");
        
        denuncia.setAtualizadoEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
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
        OffsetDateTime agora = OffsetDateTime.now(DateUtils.FUSO_BRASIL);

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
