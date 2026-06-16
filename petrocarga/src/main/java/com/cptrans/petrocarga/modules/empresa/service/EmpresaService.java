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
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.exceptions.GlobalHandlerException;
import com.cptrans.petrocarga.shared.utils.DateUtils;

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
        return empresaRepository.findById(id).orElseThrow(() -> new EmpresaExceptions.EmpresaNotFoundException());
    }

    public Empresa findByUsuarioId(UUID usuarioId) {
        return empresaRepository.findByUsuarioIdAndUsuarioAtivoTrue(usuarioId).orElseThrow(() -> new EmpresaExceptions.EmpresaNotFoundException());
    }

    public void desativarEmpresa(UUID usuarioId) {
        Empresa empresa = findByUsuarioId(usuarioId);
        empresa.getUsuario().setAtivo(false);
        empresa.getUsuario().setDesativadoEm(DateUtils.agora());
        empresaRepository.save(empresa);
    }

    @Transactional
    public EmpresaResponseDTO create(EmpresaRequestDTO request) {
        if (request.getAceitouTermos().equals(Boolean.FALSE)) throw new GlobalHandlerException.TermosNotAcceptedException();

        if (empresaRepository.existsByCnpj(request.getCnpj())) throw new EmpresaExceptions.CnpjAlreadyExistsException();

        Usuario usuarioSalvo = usuarioService.createUsuario(request.getUsuario(), PermissaoEnum.EMPRESA); 
        
        Empresa novaEmpresa = new Empresa();

        novaEmpresa.setUsuario(usuarioSalvo);
        novaEmpresa.setCnpj(request.getCnpj());
        novaEmpresa.setRazaoSocial(request.getRazaoSocial());

        Empresa empresaSalva = empresaRepository.save(novaEmpresa);
        return EmpresaMapper.toResponse(empresaSalva);
    }

    @Transactional
    public EmpresaResponseDTO update(UUID usuarioId, UsuarioPATCHRequestDTO request) {
        Empresa empresa = findByUsuarioId(usuarioId);
        Usuario usuarioAtualizado = usuarioService.patchUpdate(usuarioId, PermissaoEnum.EMPRESA, request);
        empresa.setUsuario(usuarioAtualizado);

        if (request.getCnpj() != null && !request.getCnpj().equals(empresa.getCnpj())) {
            if (empresaRepository.existsByCnpjAndIdNot(request.getCnpj(), empresa.getId())){
                throw new EmpresaExceptions.CnpjAlreadyExistsException();
            }
            empresa.setCnpj(request.getCnpj());
        }

        if (request.getRazaoSocial() != null && !request.getRazaoSocial().equals(empresa.getRazaoSocial())) {
            empresa.setRazaoSocial(request.getRazaoSocial());
        }

        Empresa empresaSalva = empresaRepository.save(empresa);
        return EmpresaMapper.toResponse(empresaSalva);
    }
}
