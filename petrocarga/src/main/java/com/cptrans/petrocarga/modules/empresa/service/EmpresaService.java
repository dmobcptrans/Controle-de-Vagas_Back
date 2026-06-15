package com.cptrans.petrocarga.modules.empresa.service;

import java.util.List;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.empresa.dto.mapper.EmpresaMapper;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.exceptions.EmpresaExceptions;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.empresa.specification.EmpresaSpecification;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.exceptions.GlobalHandlerException;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final Sort SORT_ASC = Sort.by("razaoSocial").ascending();
    private final Sort SORT_DESC = Sort.by("razaoSocial").descending();

    public List<Empresa> findAll() {
        return empresaRepository.findAll();
    }

    public PageResponseDTO listarEmpresas(EmpresaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem.equals(OrdemEnum.ASC) ? SORT_ASC : SORT_DESC);
        Page<Empresa> page = empresaRepository.findAll(EmpresaSpecification.filtrar(filtros), pageable);
        if (page.isEmpty()) return new PageResponseDTO(page);
        Page<EmpresaResponseDTO> pageResponse = page.map(EmpresaMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }

    public Empresa findById(UUID id) {
        return empresaRepository.findById(id).orElseThrow(() ->new EntityNotFoundException("Empresa não encontrada."));
    }

    public Empresa findByUsuarioId(UUID usuarioId) {
        return empresaRepository.findByUsuarioId(usuarioId).orElseThrow(() -> new EntityNotFoundException("Empresa nao encontrada."));
    }

    public void deleteById(UUID id) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Empresa nao encontrada."));
        empresaRepository.deleteById(empresa.getId());
    }

    @Transactional
    public EmpresaResponseDTO create(EmpresaRequestDTO request) {
        if (request.getAceitouTermos().equals(Boolean.FALSE)) throw new GlobalHandlerException.TermosNotAcceptedException();

        if (empresaRepository.existsByCnpj(request.getCnpj())) throw new EmpresaExceptions.CnpjAlreadyExistsException();

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.getNome());
        novoUsuario.setEmailHash(request.getEmail());
        novoUsuario.setAceitarTermos(request.getAceitouTermos());
        novoUsuario.setAceitouTermosEm(DateUtils.agora());
        novoUsuario.setTelefoneHash(request.getTelefone());
        novoUsuario.setSenha(request.getSenha());

        Usuario usuarioSalvo = usuarioService.createUsuario(novoUsuario, PermissaoEnum.EMPRESA, request.getCpf()); 
        
        Empresa novaEmpresa = new Empresa();

        novaEmpresa.setUsuario(usuarioSalvo);
        novaEmpresa.setCnpj(request.getCnpj());
        novaEmpresa.setRazaoSocial(request.getRazaoSocial());

        Empresa empresaSalva = empresaRepository.save(novaEmpresa);
        return EmpresaMapper.toResponse(empresaSalva);
    }
}
