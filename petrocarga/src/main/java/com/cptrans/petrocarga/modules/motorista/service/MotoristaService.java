package com.cptrans.petrocarga.modules.motorista.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.TipoCnhEnum;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.service.EmpresaService;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaFiltrosDTO;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.exceptions.MotoristaExceptions;
import com.cptrans.petrocarga.modules.motorista.repository.MotoristaRepository;
import com.cptrans.petrocarga.modules.motorista.specification.MotoristaSpecification;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.security.UserAuthenticated;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final UsuarioService usuarioService;
    private final HashService hashService;
    private final CriptoService criptoService;
    private final EmpresaService empresaService;

    public List<Motorista> findAll() {
        return motoristaRepository.findAll();
    }

    public List<Motorista> findAllWithFiltros(MotoristaFiltrosDTO filtros) {
        return motoristaRepository.findAll(MotoristaSpecification.filtrar(filtros));
    }

    public Motorista findByUsuarioIdAndAtivo(UUID usuarioId, Boolean ativo) {
        if(ativo == null) ativo = true;
        Motorista motorista = motoristaRepository.findByUsuarioIdAndUsuarioAtivo(usuarioId, ativo)
                .orElseThrow(() -> new IllegalArgumentException("Motorista não encontrado"));
        return motorista;
    }

    public Motorista findByUsuarioId(UUID usuarioId) {
        Motorista motorista = motoristaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Motorista não encontrado"));
        return motorista;
    }

    @Transactional
    public Motorista createMotorista(MotoristaRequestDTO request) {
        Motorista novoMotorista = new Motorista();

        if (request.getEmpresaId() != null) {
            UserAuthenticated userAuthenticated =  !SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser") ? (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
            if (userAuthenticated == null) throw new AuthExceptions.UsuarioNaoAutenticadoException();
            List<String> authorities = userAuthenticated.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            if (!authorities.contains(PermissaoEnum.EMPRESA.getRole()) && !authorities.contains(PermissaoEnum.ADMIN.getRole())) throw new AuthExceptions.UsuarioNaoAutorizadoException();
            Empresa empresa = empresaService.findById(request.getEmpresaId());
            if (authorities.contains(PermissaoEnum.EMPRESA.getRole()) && !userAuthenticated.id().equals(empresa.getUsuario().getId())) throw new AuthExceptions.UsuarioNaoAutorizadoException();
            novoMotorista.setEmpresa(empresa);
        }

        Usuario usuario = usuarioService.createUsuario(request.getUsuario(), PermissaoEnum.MOTORISTA);
        novoMotorista.setUsuario(usuario);

        String cnhString = request.getNumeroCnh().trim();
        String cnhHash = hashService.hash(cnhString);
        
        if (motoristaRepository.existsByCnhHash(cnhHash)) throw new MotoristaExceptions.CnhAlreadyExistsException();
        
        String cnhCripto = criptoService.encrypt(cnhString);
        novoMotorista.setCnhHash(cnhHash);
        novoMotorista.setCnhCripto(cnhCripto);
        novoMotorista.setCnhLast4(UsuarioUtils.gerarLastN(cnhString, 4));

        novoMotorista.setDataValidadeCnh(request.getDataValidadeCnh());
        novoMotorista.setTipoCnh(request.getTipoCnh());
        return  motoristaRepository.save(novoMotorista);
    }

    @Transactional
    public Motorista updateMotorista(UserAuthenticated usuarioAutenticado, UUID usuarioId, UsuarioPATCHRequestDTO motoristaRequest) {
        Motorista motoristaCadastrado = findByUsuarioIdAndAtivo(usuarioId, true);
        
        if(motoristaRequest.getDataValidadeCnh() != null) {
            if(motoristaRequest.getDataValidadeCnh().isBefore(LocalDate.now())) throw new IllegalArgumentException("Cnh vencida");
            motoristaCadastrado.setDataValidadeCnh(motoristaRequest.getDataValidadeCnh());
        }
        if (motoristaRequest.getNumeroCnh() != null) {
            Optional<Motorista> motoristaByCnh = motoristaRepository.findByCnhHash(hashService.hash(motoristaRequest.getNumeroCnh()));
            if(motoristaByCnh.isPresent() && !motoristaByCnh.get().getId().equals(motoristaCadastrado.getId())){
                throw new IllegalArgumentException("Número da Cnh já cadastrado");
            }
            motoristaCadastrado.setCnhHash(hashService.hash(motoristaRequest.getNumeroCnh()));
            motoristaCadastrado.setCnhCripto(criptoService.encrypt(motoristaRequest.getNumeroCnh()));
            motoristaCadastrado.setCnhLast4(UsuarioUtils.gerarLastN(motoristaRequest.getNumeroCnh(), 4));
        }
        if (motoristaRequest.getTipoCnh() != null) {
            motoristaCadastrado.setTipoCnh(motoristaRequest.getTipoCnh());
        }

        if (motoristaRequest.getEmpresaId() != null) {
            Empresa empresa = empresaService.findById(motoristaRequest.getEmpresaId());
            motoristaCadastrado.setEmpresa(empresa);
        }

        Usuario usuarioAtualizado = usuarioService.patchUpdate(usuarioId, PermissaoEnum.MOTORISTA, motoristaRequest);
        motoristaCadastrado.setUsuario(usuarioAtualizado);

        return motoristaRepository.save(motoristaCadastrado);
    }

    public Motorista findById(UUID id) {
        return motoristaRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("Motorista nao encontrado."));
    }

    public void deleteByUsuarioId(UUID usuarioId) {
        Motorista motorista = findByUsuarioIdAndAtivo(usuarioId, true);
        usuarioService.deleteById(motorista.getUsuario().getId());
    }

    public Motorista completarCadastro(Usuario usuario, String numeroCnh, LocalDate dataValidadeCnh, TipoCnhEnum tipoCnh){
        Optional<Motorista> motorista = motoristaRepository.findByUsuarioId(usuario.getId());
        
        if(motorista.isPresent()){
            Optional<Motorista> motoristaByCnh = motoristaRepository.findByCnhHash(hashService.hash(numeroCnh));
            if(motoristaByCnh.isPresent() && !motoristaByCnh.get().getUsuario().getId().equals(usuario.getId())){
                throw new IllegalArgumentException("CNH já cadastrada");
            }
            else{
                motorista.get().setDataValidadeCnh(dataValidadeCnh);
                motorista.get().setCnhHash(hashService.hash(numeroCnh));
                motorista.get().setCnhCripto(criptoService.encrypt(numeroCnh));
                motorista.get().setCnhLast4(UsuarioUtils.gerarLastN(numeroCnh, 4));
                motorista.get().setTipoCnh(tipoCnh);
                return motoristaRepository.save(motorista.get());
            }
        }else{
            Motorista novoMotorista = new Motorista();
            novoMotorista.setDataValidadeCnh(dataValidadeCnh);
            novoMotorista.setCnhHash(hashService.hash(numeroCnh));
            novoMotorista.setCnhCripto(criptoService.encrypt(numeroCnh));
            novoMotorista.setCnhLast4(UsuarioUtils.gerarLastN(numeroCnh, 4));
            novoMotorista.setTipoCnh(tipoCnh);
            novoMotorista.setUsuario(usuario);
            return motoristaRepository.save(novoMotorista);
        }
    }
}