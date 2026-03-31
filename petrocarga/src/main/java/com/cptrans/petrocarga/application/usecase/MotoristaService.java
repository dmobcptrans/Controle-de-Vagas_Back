package com.cptrans.petrocarga.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.MotoristaFiltrosDTO;
import com.cptrans.petrocarga.application.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.TipoCnhEnum;
import com.cptrans.petrocarga.domain.repositories.MotoristaRepository;
import com.cptrans.petrocarga.domain.specification.MotoristaSpecification;
import com.cptrans.petrocarga.infrastructure.security.CriptoService;
import com.cptrans.petrocarga.infrastructure.security.HashService;
import com.cptrans.petrocarga.shared.utils.UsuarioUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class MotoristaService {

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private HashService hashService;

    @Autowired
    private CriptoService criptoService;

    // @Autowired
    // private EmpresaService empresaService;

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
    public Motorista createMotorista(Motorista novoMotorista) {
        // if(novoMotorista.getEmpresa() != null) {
            //     Empresa empresa = empresaService.findById(novoMotorista.getEmpresa().getId());
            //     novoMotorista.setEmpresa(empresa);
            // }
            if(novoMotorista.getDataValidadeCnh().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("CNH vencida");
            }
            Usuario usuario = usuarioService.createUsuario(novoMotorista.getUsuario(), PermissaoEnum.MOTORISTA, novoMotorista.getUsuario().getCpfHash());
            novoMotorista.setUsuario(usuario);
            if(motoristaRepository.existsByCnhHash(novoMotorista.getCnhHash())) {
                throw new IllegalArgumentException("Número da CNH já cadastrado");
            }
            String numero_cnh = novoMotorista.getCnhHash();
            novoMotorista.setCnhHash(hashService.hash(numero_cnh));
            novoMotorista.setCnhCripto(criptoService.encrypt(numero_cnh));
            novoMotorista.setCnhLast4(UsuarioUtils.gerarLastN(numero_cnh, 4));
        return  motoristaRepository.save(novoMotorista);
    }

    @Transactional
    public Motorista updateMotorista(UUID usuarioId, UsuarioPATCHRequestDTO motoristaRequest) {
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

        // if (motoristaRequest.getEmpresa() != null) {
        //     Empresa empresa = empresaService.findById(motoristaRequest.getEmpresa().getId());
        //     //TODO: Criar lógica de update no EmpresaService
        //     motoristaCadastrado.setEmpresa(empresa);
        // }

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