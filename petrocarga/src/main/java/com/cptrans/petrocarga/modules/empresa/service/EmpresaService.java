package com.cptrans.petrocarga.modules.empresa.service;


import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.empresa.dto.mapper.EmpresaMapper;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaRequestDTO;
import com.cptrans.petrocarga.modules.empresa.dto.response.EmpresaResponseDTO;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.exceptions.EmpresaExceptions;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.empresa.specification.EmpresaSpecification;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.exceptions.UsuarioExceptions;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
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
    private final HashService hashService;
    private final EmpresaMapper empresaMapper;
    private final Sort SORT_ASC = Sort.by("usuario.nome").ascending();
    private final Sort SORT_DESC = Sort.by("usuario.nome").descending();

    public PageResponseDTO listarEmpresas(EmpresaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        if (filtros != null && filtros.getTelefone() != null ) filtros.setTelefone(hashService.hash(filtros.getTelefone().trim()));
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem.equals(OrdemEnum.ASC) ? SORT_ASC : SORT_DESC);
        Page<Empresa> page = empresaRepository.findAll(EmpresaSpecification.filtrar(filtros), pageable);
        if (page.isEmpty()) return new PageResponseDTO(page);
        Page<EmpresaResponseDTO> pageResponse = page.map(empresaMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }

    public Empresa findByIdAndAtivoTrue(UUID id) {
        return empresaRepository.findByIdAndUsuarioAtivoTrue(id).orElseThrow(() -> new EmpresaExceptions.EmpresaNotFoundException());
    }

    public void desativarEmpresa(UUID usuarioId) {
        Empresa empresa = findByIdAndAtivoTrue(usuarioId);
        empresa.getUsuario().setAtivo(false);
        empresa.getUsuario().setDesativadoEm(DateUtils.agora());
        empresaRepository.save(empresa);
    }

    @Transactional
    public EmpresaResponseDTO create(EmpresaRequestDTO request) {
        if (request.getAceitouTermos() != null && !request.getAceitouTermos()) throw new UsuarioExceptions.TermosNotAcceptedException();

        String cnpj = request.getCnpj().trim();
        if (empresaRepository.existsByCnpj(cnpj)) throw new EmpresaExceptions.CnpjAlreadyExistsException();

        Usuario usuarioSalvo = usuarioService.createUsuario(new UsuarioRequestDTO(request.getNome(), request.getTelefone(), request.getEmail(), request.getSenha(), request.getAceitouTermos()), null, PermissaoEnum.EMPRESA);
        
        Empresa novaEmpresa = new Empresa();

        novaEmpresa.setUsuario(usuarioSalvo);
        novaEmpresa.setCnpj(cnpj);

        Empresa empresaSalva = empresaRepository.save(novaEmpresa);
        return empresaMapper.toResponse(empresaSalva);
    }

    @Transactional
    public EmpresaResponseDTO update(UUID usuarioId, UsuarioPATCHRequestDTO request) {
        Empresa empresa = findByIdAndAtivoTrue(usuarioId);
        Usuario usuarioAtualizado = usuarioService.patchUpdate(usuarioId, PermissaoEnum.EMPRESA, request);
        empresa.setUsuario(usuarioAtualizado);

        if (request.getCnpj() != null && !request.getCnpj().equals(empresa.getCnpj())) {
            if (empresaRepository.existsByCnpjAndIdNot(request.getCnpj(), empresa.getId())){
                throw new EmpresaExceptions.CnpjAlreadyExistsException();
            }
            empresa.setCnpj(request.getCnpj());
        }

        Empresa empresaSalva = empresaRepository.save(empresa);
        return empresaMapper.toResponse(empresaSalva);
    }
}