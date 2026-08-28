package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.StatusConviteMotoristaEmpresaEnum;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.mapper.ConviteMotoristaEmpresaMapper;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.ConviteMotoristaEmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.ConviteMotoristaEmpresaRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.RespostaConviteMotoristaExistenteRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.RespostaConviteNovoMotoristaRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.response.ConviteMotoristaEmpresaResponseDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.entity.ConviteMotoristaEmpresa;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.exceptions.ConviteMotoristaEmpresaExceptions;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.repository.ConviteMotoristaEmpresaRepository;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.specification.ConviteMotoristaEmpresaSpecification;
import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.exceptions.EmpresaExceptions;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.events.SpringDomainEventPublisher;
import com.cptrans.petrocarga.modules.events.conviteMotoristaEmpresa.ConviteCriadoEvent;
import com.cptrans.petrocarga.modules.messaging.email.EmailSender;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaEmpresaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.exceptions.MotoristaExceptions;
import com.cptrans.petrocarga.modules.motorista.service.MotoristaService;
import com.cptrans.petrocarga.modules.notificacao.service.NotificacaoService;
import com.cptrans.petrocarga.modules.usuario.exceptions.UsuarioExceptions;
import com.cptrans.petrocarga.modules.usuario.repository.UsuarioRepository;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConviteMotoristaEmpresaService {
    private final ConviteMotoristaEmpresaRepository repository;
    private final ConviteMotoristaEmpresaMapper mapper;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MotoristaService motoristaService;
    private final HashService hashService;
    private final CriptoService criptoService;
    private final NotificacaoService notificacaoService;
    private final EmailSender emailSender;
    private final SpringDomainEventPublisher eventPublisher;

    private final Sort SORT_ASC = Sort.by("criadoEm").ascending();
    private final Sort SORT_DESC = Sort.by("criadoEm").descending();


    public ConviteMotoristaEmpresaResponseDTO getByToken(String token) {
        ConviteMotoristaEmpresa convite = findValidoByToken(token);
        return mapper.toResponse(convite, false);
    }

    public PageResponseDTO getConvitesByEmpresa(ConviteMotoristaEmpresaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        if (filtros == null || filtros.getEmpresaId() == null) throw new EmpresaExceptions.EmpresaNotFoundException();
        
        if (filtros != null && filtros.getMotoristaEmail() != null && !filtros.getMotoristaEmail().trim().isEmpty()) {
            filtros.setMotoristaEmail(hashService.hash(filtros.getMotoristaEmail().trim().toLowerCase()));
        }

        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem.equals(OrdemEnum.ASC) ? SORT_ASC : SORT_DESC);
        Page<ConviteMotoristaEmpresa> page = repository.findAll(ConviteMotoristaEmpresaSpecification.filtrar(filtros), pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);

        return new PageResponseDTO(page.map(c -> mapper.toResponse(c, true))); 
    }

    public PageResponseDTO getConvitesByMotorista(ConviteMotoristaEmpresaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        if (filtros == null || filtros.getMotoristaId() == null) throw new MotoristaExceptions.MotoristaNotFoundException();
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem.equals(OrdemEnum.ASC) ? SORT_ASC : SORT_DESC);
        Page<ConviteMotoristaEmpresa> page = repository.findAll(ConviteMotoristaEmpresaSpecification.filtrar(filtros), pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);

        return new PageResponseDTO(page.map(c -> mapper.toResponse(c, true))); 
    }

    @Transactional
    public void convidarMotorista(UUID empresaId, ConviteMotoristaEmpresaRequestDTO request) {
        Empresa empresa = findEmpresaById(empresaId);
        String motoristaEmail = request.getEmailMotorista().trim().toLowerCase();
        String emailHash = hashService.hash(motoristaEmail);

        if (repository.existsValidoByMotoristaEmailHashAndEmpresaId(emailHash, empresaId)) throw new ConviteMotoristaEmpresaExceptions.ConviteAlreadyExistsException();

        Motorista motoristaByEmail = motoristaService.findOptionalByEmailHash(emailHash);

        ConviteMotoristaEmpresa convite;

        if (motoristaByEmail != null) {
            convite = criarConviteParaMotoristaExistente(motoristaByEmail, empresa, motoristaEmail);
            notificacaoService.notificarConviteEmpresaMotorista(empresa.getUsuario().getNome(), motoristaByEmail.getId());
        } else convite = criarConviteParaNovoMotorista(motoristaEmail, empresa);

        String token = convite.gerarToken(hashService);
        repository.save(convite);
        
        emailSender.sendConviteMotoristaEmpresa(motoristaEmail, convite, request.getNomeMotorista(), token);
        eventPublisher.publish(new ConviteCriadoEvent(convite.getId(), convite.getValidoAte()));
    }
   
    private ConviteMotoristaEmpresa criarConviteParaNovoMotorista(String motoristaEmail, Empresa empresa) {
        motoristaEmail = motoristaEmail.trim().toLowerCase();
        
        String emailHash = hashService.hash(motoristaEmail);
       
        if (existsByEmailHashAndIsNotMotorista(emailHash)) throw new UsuarioExceptions.EmailAlreadyExistsException();
        
        String emailCripto = criptoService.encrypt(motoristaEmail);
        
        return repository.save(new ConviteMotoristaEmpresa(empresa, emailHash, emailCripto, criptoService.getActiveKeyVersion()));

    }

    private ConviteMotoristaEmpresa criarConviteParaMotoristaExistente(Motorista motorista, Empresa empresa, String motoristaEmail) {
        motoristaEmail = motoristaEmail.trim().toLowerCase();
        String emailHash = hashService.hash(motoristaEmail);
        String emailCripto = criptoService.encrypt(motoristaEmail);
        
        validarMotoristaPertenceAEmpresa(motorista, empresa.getId());
        
        return repository.save(new ConviteMotoristaEmpresa(motorista, empresa, emailHash, emailCripto, criptoService.getActiveKeyVersion()));
    }

    @Transactional
    public void responderConviteNovoMotorista(RespostaConviteNovoMotoristaRequestDTO request) {
        ConviteMotoristaEmpresa convite = findValidoByToken(request.getConviteToken());
        Empresa empresa = convite.getEmpresa();
        String emailMotorista = criptoService.decrypt(convite.getMotoristaEmailCripto(), convite.getCriptoVersion());

        switch (request.getStatus()) {
            case ACEITO:
                convite.aceitar();
                Motorista novoMotorista = cadastrarMotoristaByConvite(request.getMotorista(), emailMotorista, empresa);
                convite.vincularMotorista(novoMotorista);
                repository.save(convite);
                break;
            case RECUSADO:
                convite.recusar();
                repository.save(convite);
                break;
            default:
                throw new ConviteMotoristaEmpresaExceptions.RespostaInvalidaExceptions();
        }
                
    }

    @Transactional
    public void responderConviteMotoristaExistente(UUID motoristaId, RespostaConviteMotoristaExistenteRequestDTO request) {
        ConviteMotoristaEmpresa convite = findValidoByConviteIdAndMotoristaId(request.getConviteId(), motoristaId);
        Empresa empresa = convite.getEmpresa();
        
        switch (request.getStatus()) {
            case ACEITO:
                convite.aceitar();
                Motorista motorista = convite.getMotorista();
                validarMotoristaPertenceAEmpresa(motorista, empresa.getId());
                motorista.setEmpresa(empresa);
                motoristaService.save(motorista);
                repository.save(convite);
                break;
            case RECUSADO:
                convite.recusar();
                repository.save(convite);
                break;
            default:
                throw new ConviteMotoristaEmpresaExceptions.RespostaInvalidaExceptions();
        }
                
    }

    public ConviteMotoristaEmpresa findValidoByMotoristaIdAndEmpresaId(UUID motoristaId, UUID empresaId) {
        return repository.findConviteValidoByMotoristaIdAndEmpresaId(motoristaId, empresaId).orElseThrow(() -> new ConviteMotoristaEmpresaExceptions.ConviteNotFoundException());
    }

    public void cancelarConvite(UUID empresaId, UUID conviteId) {
        ConviteMotoristaEmpresa convite = findPendenteValidoByIdAndEmpresaId(conviteId, empresaId);
        repository.delete(convite);
    }

    private ConviteMotoristaEmpresa findPendenteValidoByIdAndEmpresaId(UUID conviteId, UUID empresaId) {
        return repository.findPendenteValidoByIdAndEmpresaId(conviteId, empresaId).orElseThrow(() -> new ConviteMotoristaEmpresaExceptions.ConviteNotFoundException());
    }

    private ConviteMotoristaEmpresa findValidoByToken(String token) {
        String tokenHash = hashService.hash(token.trim());
        return repository.findValidoByTokenHash(tokenHash).orElseThrow(() -> new ConviteMotoristaEmpresaExceptions.ConviteNotFoundException());
    }

    private Empresa findEmpresaById(UUID empresaId) {
        return empresaRepository.findByIdAndUsuarioAtivoTrue(empresaId).orElse(null);
    }

    public ConviteMotoristaEmpresa findValidoByConviteIdAndMotoristaId(UUID conviteId, UUID motoristaId) {
        return repository.findConviteValidoByIdAndMotoristaId(conviteId, motoristaId).orElseThrow(() -> new ConviteMotoristaEmpresaExceptions.ConviteNotFoundException());
    }
    
    private void validarMotoristaPertenceAEmpresa(Motorista motorista, UUID empresaId) {
        if (motorista.getEmpresa() != null ){
            if (!motorista.getEmpresa().getId().equals(empresaId)) throw new MotoristaExceptions.MotoristaJaPossuiEmpresaException();
            else throw new ConviteMotoristaEmpresaExceptions.MotoristaJaVinculadoException();
        } 
    }

    private Boolean existsByEmailHashAndIsNotMotorista(String emailHash) {
        return usuarioRepository.existsByEmailHashAndPermissaoNot(emailHash, PermissaoEnum.MOTORISTA);
    }

    @Transactional
    private Motorista cadastrarMotoristaByConvite(MotoristaEmpresaRequestDTO request, String emailMotorista, Empresa empresa) {
        return motoristaService.createMotoristaByConviteEmpresa(request, emailMotorista, empresa);
    }

    public void excluirConviteExpirado(UUID conviteId) {
        ConviteMotoristaEmpresa convite = repository.findById(conviteId).orElse(null);
        if (convite == null) return;
        if (DateUtils.agora().toInstant().isAfter(convite.getValidoAte().toInstant()) || !convite.getStatus().equals(StatusConviteMotoristaEmpresaEnum.PENDENTE)){
            repository.deleteById(conviteId);
        }
    }
}