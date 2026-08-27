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
import com.cptrans.petrocarga.modules.motorista.dto.mapper.MotoristaMapper;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaEmpresaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaResumoResponseDTO;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final UsuarioService usuarioService;
    private final HashService hashService;
    private final CriptoService criptoService;
    private final MotoristaMapper motoristaMapper;
    private final ReservaUtils reservaUtils;
    private final VeiculoEmpresaMotoristaService veiculoEmpresaMotoristaService;

    private final Sort SORT_ASC = Sort.by("usuario.nome").ascending();
    private final Sort SORT_DESC = Sort.by("usuario.nome").descending();

    public Motorista save(Motorista motorista) {
        return motoristaRepository.save(motorista);
    }

    public Motorista findOptionalByEmailHash(String emailHash) {
        return motoristaRepository.findByUsuarioEmailHash(emailHash).orElse(null);
    }
    
    public Motorista findById(UUID id) {
        return motoristaRepository.findById(id).orElseThrow(()-> new MotoristaExceptions.MotoristaNotFoundException());
    }
    
    public Motorista findByIdAndAtivo(UUID id, Boolean ativo) {
        if (ativo == null) ativo = true;
        return motoristaRepository.findByIdAndUsuarioAtivo(id, ativo).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
    }

    public Motorista findByIdAndAtivoTrue(UUID id) {
        return findByIdAndAtivo(id, true);
    }

    public Motorista findByIdAndEmpresaId(UUID id, UUID empresaId) {
        return motoristaRepository.findByIdAndEmpresaId(id, empresaId).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
    }

    public PageResponseDTO findAllWithFiltros(MotoristaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, !ordem.equals(OrdemEnum.ASC) ? SORT_DESC : SORT_ASC);
        
        if (filtros != null) {
            if (filtros.getTelefone() != null && !filtros.getTelefone().isEmpty()) filtros.setTelefone(hashService.hash(filtros.getTelefone().trim()));
            if (filtros.getEmail() != null && !filtros.getEmail().isEmpty()) filtros.setEmail(hashService.hash(filtros.getEmail().trim()));
            if (filtros.getCpf() != null && !filtros.getCpf().isEmpty()) filtros.setCpf(hashService.hash(filtros.getCpf().trim()));
            if (filtros.getCnh() != null && !filtros.getCnh().isEmpty()) filtros.setCnh(hashService.hash(filtros.getCnh().trim()));
        }
        Page<Motorista> page = motoristaRepository.findAll(MotoristaSpecification.filtrar(filtros), pageable);
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        Page<MotoristaResumoResponseDTO> pageResponse = page.map(motoristaMapper::toResponseResumido);
        return new PageResponseDTO(pageResponse);
    }

    public PageResponseDTO findByEmpresaId(MotoristaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, !ordem.equals(OrdemEnum.ASC) ? SORT_DESC : SORT_ASC);
        Page<Motorista> page = motoristaRepository.findAll(MotoristaSpecification.filtrar(filtros), pageable);
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
    public Motorista createMotoristaByConviteEmpresa(
        @Valid
        @NotNull(message = "O motorista não pode ser nulo.")
        MotoristaEmpresaRequestDTO request,

        String emailMotorista,

        Empresa empresa
    ) {
        Usuario usuario = usuarioService.createMotoristaEmpresa(request, emailMotorista);
    
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
        if (reservaUtils.existsAtivaByEmpresaIdAndMotoristaId(empresaId, motoristaId)) throw new UsuarioExceptions.PossuiReservaAtivaException();
        veiculoEmpresaMotoristaService.desvincularTodosByMotoristaId(motoristaId);
        Motorista motorista = motoristaRepository.findByIdAndEmpresaId(motoristaId, empresaId).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
        motorista.setEmpresa(null);
        motoristaRepository.save(motorista);
        return new SystemResponse("Motorista desvinculado da empresa com sucesso",201);
    }

    @Transactional
    public Motorista updateMotorista(UserAuthenticated usuarioAutenticado, UUID id, UsuarioPATCHRequestDTO motoristaRequest) {
        Motorista motoristaCadastrado = findByIdAndAtivoTrue(id);
        
        Usuario usuarioAtualizado = usuarioService.patchUpdate(id, PermissaoEnum.MOTORISTA, motoristaRequest);
        
        if (motoristaRequest.getDataValidadeCnh() != null) {
            if (motoristaRequest.getDataValidadeCnh().isBefore(LocalDate.now())) throw new MotoristaExceptions.CnhVencidaException();
            motoristaCadastrado.setDataValidadeCnh(motoristaRequest.getDataValidadeCnh());
        }
        
        if (motoristaRequest.getNumeroCnh() != null) {
            String cnh = motoristaRequest.getNumeroCnh().trim();
            String cnhHash = hashService.hash(cnh);
            if (motoristaRepository.existsByCnhHashAndIdNot(cnhHash, id)) throw new MotoristaExceptions.CnhAlreadyExistsException();
            motoristaCadastrado.setCnhHash(cnhHash);
            motoristaCadastrado.setCnhCripto(criptoService.encrypt(cnh));
            motoristaCadastrado.setCnhLast4(UsuarioUtils.gerarLastN(cnh, 4));
        }

        if (motoristaRequest.getTipoCnh() != null) motoristaCadastrado.setTipoCnh(motoristaRequest.getTipoCnh());

        if (motoristaRequest.getCpf() != null) {
            String cpf = motoristaRequest.getCpf().trim();
            String cpfHash = hashService.hash(cpf);
            if (motoristaRepository.existsByCpfHashAndIdNot(cpfHash, id)) throw new UsuarioExceptions.CpfAlreadyExistsException();
            motoristaCadastrado.setCpfHash(cpfHash);
            motoristaCadastrado.setCpfCripto(criptoService.encrypt(cpf));
            motoristaCadastrado.setCpfLast5(UsuarioUtils.gerarLastN(cpf, 5));
        }

        motoristaCadastrado.setUsuario(usuarioAtualizado);

        return motoristaRepository.save(motoristaCadastrado);
    }

    public void desativarById(UUID id) {
        usuarioService.desativarById(id);
    }

    @Transactional
    public Motorista completarCadastro(Usuario usuario, String numeroCnh, String cpf, LocalDate dataValidadeCnh, TipoCnhEnum tipoCnh){
        if (motoristaRepository.existsByCnhHashAndIdNot(numeroCnh.trim(), usuario.getId())) throw new MotoristaExceptions.CnhAlreadyExistsException();
        
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
        } else {
            Motorista novoMotorista = instanciarMotorista(usuario, numeroCnh, tipoCnh, dataValidadeCnh, cpf);
            return motoristaRepository.save(novoMotorista);
        }

    }

    private Motorista instanciarMotorista(Usuario usuario, String cnh, TipoCnhEnum tipoCnh, LocalDate dataValidadeCnh, String cpf) {
        cnh = cnh.trim();
        String cnhHash = hashService.hash(cnh);
        String cnhCripto = criptoService.encrypt(cnh);
        String cnhLast4 = UsuarioUtils.gerarLastN(cnh, 4);

        cpf = cpf.trim();
        String cpfHash = hashService.hash(cpf);
        String cpfCripto = criptoService.encrypt(cpf);
        String cpfLast5 = UsuarioUtils.gerarLastN(cpf, 5);

        Motorista motorista = new Motorista(
            usuario,
            tipoCnh,
            dataValidadeCnh,
            cnhHash,
            cnhCripto,
            cnhLast4,
            cpfHash,
            cpfCripto,
            cpfLast5
        );

        return motorista;
    }
}