package com.cptrans.petrocarga.modules.motorista.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.TipoCnhEnum;
import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.service.EmpresaService;
import com.cptrans.petrocarga.modules.motorista.dto.mapper.MotoristaMapper;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaEmpresaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaFiltrosDTO;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResponseDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.exceptions.MotoristaExceptions;
import com.cptrans.petrocarga.modules.motorista.repository.MotoristaRepository;
import com.cptrans.petrocarga.modules.motorista.specification.MotoristaSpecification;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.exceptions.UsuarioExceptions;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.service.VeiculoEmpresaMotoristaService;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

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
    private final MotoristaMapper motoristaMapper;
    private final ReservaUtils reservaUtils;
    private final VeiculoEmpresaMotoristaService veiculoEmpresaMotoristaService;

    private final Sort SORT_ASC = Sort.by("usuario.nome").ascending();
    private final Sort SORT_DESC = Sort.by("usuario.nome").descending();

    public Motorista findByIdAndAtivo(UUID id, Boolean ativo) {
        if (ativo == null) ativo = true;
        return motoristaRepository.findByIdAndUsuarioAtivo(id, ativo).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
    }

    public Motorista findByIdAndAtivoTrue(UUID id) {
        return findByIdAndAtivo(id, true);
    }

    public PageResponseDTO findAllWithFiltros(MotoristaFiltrosDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, !ordem.equals(OrdemEnum.ASC) ? SORT_DESC : SORT_ASC);
        
        if (filtros != null) {
            if (filtros.getTelefone() != null && !filtros.getTelefone().isEmpty()) filtros.setTelefone(hashService.hash(filtros.getTelefone().trim()));
            if (filtros.getEmail() != null && !filtros.getEmail().isEmpty()) filtros.setEmail(hashService.hash(filtros.getEmail().trim()));
            if (filtros.getCpf() != null && !filtros.getCpf().isEmpty()) filtros.setCpf(hashService.hash(filtros.getCpf().trim()));
            if (filtros.getCnh() != null && !filtros.getCnh().isEmpty()) filtros.setCnh(hashService.hash(filtros.getCnh().trim()));
        }
        Page<Motorista> page = motoristaRepository.findAll(MotoristaSpecification.filtrar(filtros), pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        Page<MotoristaResponseDTO> pageResponse = page.map(motoristaMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }

    public PageResponseDTO findByEmpresaId(UUID empresaId, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, !ordem.equals(OrdemEnum.ASC) ? SORT_DESC : SORT_ASC);
        Page<Motorista> page = motoristaRepository.findByEmpresaId(empresaId, pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        Page<MotoristaSimplificadoResponseDTO> pageResponse = page.map(motoristaMapper::toResponseSimplificado);
        return new PageResponseDTO(pageResponse);
    }

    @Transactional
    public Motorista createMotorista(MotoristaRequestDTO request) {
        
        if (motoristaRepository.existsByCnhHash(hashService.hash(request.getNumeroCnh().trim()))) throw new MotoristaExceptions.CnhAlreadyExistsException();
        
        Usuario usuario = usuarioService.createUsuario(request.getUsuario(), request.getCpf(), PermissaoEnum.MOTORISTA);
        
        Motorista novoMotorista = instanciarMotorista(
            usuario, 
            request.getNumeroCnh(), 
            request.getTipoCnh(), 
            request.getDataValidadeCnh(), 
            request.getCpf()
        );
        
        return  motoristaRepository.save(novoMotorista);
    }

    @Transactional
    public Motorista createMotoristaByEmpresa(UUID empresaId, MotoristaEmpresaRequestDTO request) {
        Empresa empresa = empresaService.findByIdAndAtivoTrue(empresaId);
        
        Optional<Motorista> motoristaByCpfOptional = motoristaRepository.findByCpfHash(hashService.hash(request.getCpf().trim()));
        if (motoristaByCpfOptional.isPresent()) {
            Motorista motorista = motoristaByCpfOptional.get();
            if (motorista.getEmpresa() != null && !motorista.getEmpresa().getId().equals(empresa.getId())) throw new MotoristaExceptions.MotoristaJaPossuiEmpresaException();
            if (!motorista.getUsuario().getAtivo()) throw new MotoristaExceptions.MotoristaCadastradadoInativoException();
            motorista.setEmpresa(empresa);
            return motoristaRepository.save(motorista);
        }

        Optional<Motorista> motoristaByEmailOptional = motoristaRepository.findByUsuarioEmailHash(hashService.hash(request.getEmail().trim().toLowerCase()));
        if (motoristaByEmailOptional.isPresent()) {
            Motorista motorista = motoristaByEmailOptional.get();
            if (motorista.getEmpresa() != null && !motorista.getEmpresa().getId().equals(empresa.getId())) throw new MotoristaExceptions.MotoristaJaPossuiEmpresaException();
            if (!motorista.getUsuario().getAtivo()) throw new MotoristaExceptions.MotoristaCadastradadoInativoException();
            motorista.setEmpresa(empresa);
            return motoristaRepository.save(motorista);
        }

        Optional<Motorista> motoristaByCnhOptional = motoristaRepository.findByCnhHash(hashService.hash(request.getNumeroCnh().trim()));
        if (motoristaByCnhOptional.isPresent()) {
            Motorista motorista = motoristaByCnhOptional.get();
            if (motorista.getEmpresa() != null && !motorista.getEmpresa().getId().equals(empresa.getId())) throw new MotoristaExceptions.MotoristaJaPossuiEmpresaException();
            if (!motorista.getUsuario().getAtivo()) throw new MotoristaExceptions.MotoristaCadastradadoInativoException();
            motorista.setEmpresa(empresa);
            return motoristaRepository.save(motorista);
        }
    
        Usuario usuario = usuarioService.createMotoristaEmpresa(request);
    
        Motorista novoMotorista = instanciarMotorista(
            usuario, 
            request.getNumeroCnh(), 
            request.getTipoCnh(), 
            request.getDataValidadeCnh(), 
            request.getCpf()
        );

        novoMotorista.setEmpresa(empresa);

        return  motoristaRepository.save(novoMotorista);
    }

    @Transactional
    public SystemResponse desvincularMotoristaEmpresa(UUID empresaId, UUID motoristaId) {
        if (reservaUtils.existsAtivaByEmpresaIdAndMotoristaId(empresaId, motoristaId)) {
            throw new UsuarioExceptions.PossuiReservaAtivaException();
        }
        veiculoEmpresaMotoristaService.desvincularTodosByMotoristaId(motoristaId);
        Motorista motorista = motoristaRepository.findByIdAndEmpresaId(motoristaId, empresaId).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
        motorista.setEmpresa(null);
        motoristaRepository.save(motorista);
        return new SystemResponse("Motorista desvinculado da empresa com sucesso",201);
    }

    @Transactional
    public Motorista updateMotorista(UserAuthenticated usuarioAutenticado, UUID id, UsuarioPATCHRequestDTO motoristaRequest) {
        Motorista motoristaCadastrado = findByIdAndAtivo(id, true);
        
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

        // if (motoristaRequest.getEmpresaId() != null) {
        //     Empresa empresa = empresaService.findByIdAndAtivoTrue(motoristaRequest.getEmpresaId());
        //     motoristaCadastrado.setEmpresa(empresa);
        // }

        Usuario usuarioAtualizado = usuarioService.patchUpdate(id, PermissaoEnum.MOTORISTA, motoristaRequest);
        motoristaCadastrado.setUsuario(usuarioAtualizado);

        return motoristaRepository.save(motoristaCadastrado);
    }

    public Motorista findById(UUID id) {
        return motoristaRepository.findById(id).orElseThrow(()-> new MotoristaExceptions.MotoristaNotFoundException());
    }

    public void desativarById(UUID id) {
        usuarioService.desativarById(id);
    }

    // private Motorista associarMotoristaEmpresa (String motoristaCpfHash, Empresa empresa) {
    //     if (motoristaCpfHash == null || motoristaCpfHash.trim().isEmpty()) return null;
    //     Motorista motorista = motoristaRepository.findByUsuarioCpfHashAndUsuarioAtivoTrue(motoristaCpfHash).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
    //     if (motorista.getEmpresa() != null){
    //         if (!motorista.getEmpresa().getId().equals(empresa.getId())) throw new MotoristaExceptions.MotoristaJaPossuiEmpresaException();
    //         return motorista;
    //     }
    //     motorista.setEmpresa(empresa);
    //     return motoristaRepository.save(motorista);
    // }


    @Transactional
    public Motorista completarCadastro(Usuario usuario, String numeroCnh, String cpf, LocalDate dataValidadeCnh, TipoCnhEnum tipoCnh){
        if (motoristaRepository.existsByCnhHashAndIdNot(numeroCnh, usuario.getId())) throw new MotoristaExceptions.CnhAlreadyExistsException();
        
        Optional<Motorista> motoristaOptional = motoristaRepository.findById(usuario.getId());
        
        if (motoristaOptional.isPresent()){
            Motorista motorista = motoristaOptional.get();
            numeroCnh = numeroCnh.trim();
            cpf = cpf.trim();
            motorista.setDataValidadeCnh(dataValidadeCnh);
            motorista.setTipoCnh(tipoCnh); 
            motorista.setCnhHash(hashService.hash(numeroCnh));
            motorista.setCnhCripto(criptoService.encrypt(numeroCnh));
            motorista.setCnhLast4(UsuarioUtils.gerarLastN(numeroCnh, 4));
            motorista.setCpfHash(hashService.hash(cpf));
            motorista.setCpfCripto(criptoService.encrypt(cpf));
            motorista.setCpfLast5(UsuarioUtils.gerarLastN(cpf, 5));
            return motoristaRepository.save(motorista);
        } else{
            Motorista novoMotorista = new Motorista();
            novoMotorista.setDataValidadeCnh(dataValidadeCnh);
            novoMotorista.setTipoCnh(tipoCnh);
            novoMotorista.setCnhHash(hashService.hash(numeroCnh));
            novoMotorista.setCnhCripto(criptoService.encrypt(numeroCnh));
            novoMotorista.setCnhLast4(UsuarioUtils.gerarLastN(numeroCnh, 4));
            novoMotorista.setCpfHash(hashService.hash(cpf));
            novoMotorista.setCpfCripto(criptoService.encrypt(cpf));
            novoMotorista.setCpfLast5(UsuarioUtils.gerarLastN(cpf, 5));
            novoMotorista.setUsuario(usuario);
            return motoristaRepository.save(novoMotorista);
        }

    }

    private Motorista instanciarMotorista(Usuario usuario, String cnh, TipoCnhEnum tipoCnh, LocalDate dataValidadeCnh, String cpf) {
        Motorista motorista = new Motorista();
        
        cnh = cnh.trim();
        String cnhHash = hashService.hash(cnh);
        String cnhCripto = criptoService.encrypt(cnh);
        String cnhLast4 = UsuarioUtils.gerarLastN(cnh, 4);

        cpf = cpf.trim();
        String cpfHash = hashService.hash(cpf);
        String cpfCripto = criptoService.encrypt(cpf);
        String cpfLast5 = UsuarioUtils.gerarLastN(cpf, 5);

        motorista.setCnhHash(cnhHash);
        motorista.setCnhCripto(cnhCripto);
        motorista.setCnhLast4(cnhLast4);

        motorista.setTipoCnh(tipoCnh);
        motorista.setDataValidadeCnh(dataValidadeCnh);

        motorista.setCpfHash(cpfHash);
        motorista.setCpfCripto(cpfCripto);
        motorista.setCpfLast5(cpfLast5);
        motorista.setUsuario(usuario);

        return motoristaRepository.save(motorista);
    }
}