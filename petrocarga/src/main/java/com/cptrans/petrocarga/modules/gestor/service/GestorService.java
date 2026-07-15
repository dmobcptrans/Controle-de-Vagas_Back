package com.cptrans.petrocarga.modules.gestor.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.gestor.dto.mapper.GestorMapper;
import com.cptrans.petrocarga.modules.gestor.dto.request.GestorFiltrosDTO;
import com.cptrans.petrocarga.modules.gestor.dto.request.GestorRequestDTO;
import com.cptrans.petrocarga.modules.gestor.dto.response.GestorResponseDTO;
import com.cptrans.petrocarga.modules.gestor.entity.Gestor;
import com.cptrans.petrocarga.modules.gestor.exceptions.GestorExceptions;
import com.cptrans.petrocarga.modules.gestor.repository.GestorRepository;
import com.cptrans.petrocarga.modules.gestor.specification.GestorSpecification;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestorService {
    private final UsuarioService usuarioService;
    private final GestorRepository gestorRepository;
    private final HashService hashService;
    private final CriptoService criptoService;
    private final GestorMapper gestorMapper;
    private final Sort sortAsc = Sort.by("usuario.nome").ascending();
    private final Sort sortDesc = Sort.by("usuario.nome").descending();

    public List<Usuario> findAll() {
        return usuarioService.findByPermissao(PermissaoEnum.GESTOR);
    }

    public List<Usuario> findAllByAtivo(Boolean ativo) {
        return usuarioService.findByPermissaoAndAtivo(PermissaoEnum.GESTOR, ativo);
    }

    public Gestor findByUsuarioId(UUID usuarioId) {
        return gestorRepository.findById(usuarioId).orElseThrow(() -> new GestorExceptions.GestorNotFoundException());
    }

    public Gestor createGestor(GestorRequestDTO request) {
        Usuario novoUsuario = usuarioService.createUsuario(new UsuarioRequestDTO(request.getNome(), request.getTelefone(), request.getEmail(), null, false), request.getCpf(), PermissaoEnum.GESTOR);
        Gestor novoGestor = new Gestor();
        String cpfHash = hashService.hash(request.getCpf().trim());
        String cpfCripto = criptoService.encrypt(request.getCpf().trim());
        String cpfLast5 = UsuarioUtils.gerarLastN(request.getCpf().trim(), 5);
        novoGestor.setUsuario(novoUsuario);
        novoGestor.setCpfHash(cpfHash);
        novoGestor.setCpfCripto(cpfCripto);
        novoGestor.setCpfLast5(cpfLast5);
        return gestorRepository.save(novoGestor);
    }

    @Transactional
    public Gestor updateGestor(UUID id, UsuarioPATCHRequestDTO novoGestor) {
        Gestor gestor = findByUsuarioId(id);
        Usuario novoUsuario = usuarioService.patchUpdate(id, PermissaoEnum.GESTOR, novoGestor);
        gestor.setUsuario(novoUsuario);
        if (novoGestor.getCpf() != null) {
            String cpfHash = hashService.hash(novoGestor.getCpf().trim());
            String cpfCripto = criptoService.encrypt(novoGestor.getCpf().trim());
            String cpfLast5 = UsuarioUtils.gerarLastN(novoGestor.getCpf().trim(), 5);
            gestor.setCpfHash(cpfHash);
            gestor.setCpfCripto(cpfCripto);
            gestor.setCpfLast5(cpfLast5);
        }
        return gestorRepository.save(gestor);
    }

    public void desativarById(UUID usuarioId) {
        usuarioService.desativarById(usuarioId);
    }

    public PageResponseDTO findAllWithFiltros(GestorFiltrosDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        if (filtros != null){
            if (filtros.getCpf() != null) filtros.setCpf(hashService.hash(filtros.getCpf().trim()));
            if (filtros.getEmail() != null) filtros.setEmail(hashService.hash(filtros.getEmail().trim().toLowerCase()));
            if (filtros.getTelefone() != null) filtros.setTelefone(hashService.hash(filtros.getTelefone().trim()));
        }
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem != OrdemEnum.ASC ? sortDesc : sortAsc);
        Page<Gestor> page = gestorRepository.findAll(GestorSpecification.filtrar(filtros), pageable);
        if (!page.hasContent()) return new PageResponseDTO(Page.empty());
        Page<GestorResponseDTO> pageResponse = page.map(gestorMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }
}