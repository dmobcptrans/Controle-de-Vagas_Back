package com.cptrans.petrocarga.modules.veiculo.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.reserva.repository.ReservaRepository;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.modules.veiculo.dto.request.VeiculoFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.request.VeiculoRequestDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculo.exceptions.VeiculoExceptions;
import com.cptrans.petrocarga.modules.veiculo.repository.VeiculoRepository;
import com.cptrans.petrocarga.modules.veiculo.specification.VeiculoSpecification;
import com.cptrans.petrocarga.modules.veiculo.utils.VeiculoUtils;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VeiculoService {
    private final VeiculoRepository veiculoRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;
    private final HashService hashService;
    private final CriptoService criptoService;
    private final VeiculoMapper veiculoMapper;

    private final Sort SORT_ASC = Sort.by("marca").and(Sort.by("modelo").and(Sort.by("placa"))).ascending();
    private final Sort SORT_DESC = Sort.by("marca").and(Sort.by("modelo").and(Sort.by("placa"))).descending();

    public PageResponseDTO findAll(VeiculoFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, ordem != OrdemEnum.ASC ? SORT_DESC : SORT_ASC);
        
        
        if (filtros != null){
            if (filtros.getTelefoneUsuario() != null){
                String telefoneHash = hashService.hash(filtros.getTelefoneUsuario().trim());
                filtros.setTelefoneUsuario(telefoneHash);
            }
            if (filtros.getCpfProprietario() != null){
                String cpfHash = hashService.hash(filtros.getCpfProprietario().trim());
                filtros.setCpfProprietario(cpfHash);
            }
        }
       
        Page<Veiculo> page = veiculoRepository.findAll(VeiculoSpecification.filtrar(filtros), pageable);

        if (page == null || page.isEmpty() || page.getContent().isEmpty()) return new PageResponseDTO(page);
        Page<VeiculoResponseDTO> pageResponse = page.map(veiculoMapper::toResponse);
        return new PageResponseDTO(pageResponse);
    }

    public List<Veiculo> findAtivosByUsuarioId(UUID usuarioId){
        return veiculoRepository.findByUsuarioIdAndAtivoTrueAndUsuarioAtivoTrue(usuarioId);
    }

    public Veiculo findById(UUID id) {
        Veiculo veiculo = veiculoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Veiculo não encontrado."));
        UserAuthenticated usuarioLogado = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> authorities = usuarioLogado.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        if(authorities.contains(PermissaoEnum.MOTORISTA.getRole()) || authorities.contains(PermissaoEnum.EMPRESA.getRole())) {
            if(!veiculo.getUsuario().getId().equals(usuarioLogado.id())) {
                throw new IllegalArgumentException("Usuário não pode ver os veículos de outro usuário.");   
            }
        }
        return veiculoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Veiculo nao encontrado."));
    }


    public Veiculo createVeiculo (Veiculo novoVeiculo, UUID usuarioId) {
        Usuario usuarioVeiculo = usuarioService.findByIdAndAtivoTrue(usuarioId);
        String placaFormatada = VeiculoUtils.normalizarEValidar(novoVeiculo.getPlaca());
        Optional<Veiculo> veiculoByPlaca = veiculoRepository.findByPlacaAndUsuarioId(placaFormatada, usuarioId);

        UserAuthenticated usuarioLogado = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> authorities = usuarioLogado.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        
        if (!usuarioId.equals(usuarioLogado.id()) && !authorities.contains(PermissaoEnum.ADMIN.getRole())) {
            throw new IllegalArgumentException("Usuário não pode cadastrar veículos de outro usuário.");
        }

        //se for encontrado um veículo com a mesma placa, porém desativado, reativa o veiculo
        if (veiculoByPlaca.isPresent()){
            if(!veiculoByPlaca.get().getAtivo()){
                veiculoByPlaca.get().setAtivo(true);
                veiculoByPlaca.get().setDeletadoEm(null);
                return veiculoRepository.save(veiculoByPlaca.get());
            }
            throw new IllegalArgumentException("Voce já possui um veículo cadastrado com essa placa.");
        }

        if (novoVeiculo.getCpfProprietarioHash() != null){
            String cpfString= novoVeiculo.getCpfProprietarioHash();
            novoVeiculo.setCpfProprietarioHash(hashService.hash(cpfString));
            novoVeiculo.setCpfProprietarioCripto(criptoService.encrypt(cpfString));
            novoVeiculo.setCpfProprietarioLast5(UsuarioUtils.gerarLastN(cpfString, 5));
        }

        novoVeiculo.setUsuario(usuarioVeiculo);
        return veiculoRepository.save(novoVeiculo);
    }

    public Veiculo updateVeiculo(UUID veiculoId, UUID usuarioId, VeiculoRequestDTO novoVeiculo) {
        Veiculo veiculoRegistrado = findByIdAtivoAndUsuarioIdAtivo(veiculoId, usuarioId);

        if(novoVeiculo.getCpfProprietario() != null && novoVeiculo.getCnpjProprietario() != null) {
            throw new IllegalArgumentException("Veiculo não pode ter CPF e CNPJ.");
        }

        if (novoVeiculo.getPlaca() != null){
            String placaFormatada = VeiculoUtils.normalizarEValidar(novoVeiculo.getPlaca());
            Optional<Veiculo> veiculoByPlaca = veiculoRepository.findByPlacaAndUsuarioId(placaFormatada, usuarioId);
            if(veiculoByPlaca.isPresent() && !veiculoByPlaca.get().getId().equals(veiculoRegistrado.getId())) {
                if(!veiculoByPlaca.get().getAtivo()){
                    veiculoByPlaca.get().setAtivo(true);
                    veiculoByPlaca.get().setDeletadoEm(null);    
                    veiculoRepository.save(veiculoByPlaca.get());
                }
                throw new IllegalArgumentException("Você já possui um veículo cadastrado com essa placa.");
            }else{
                veiculoRegistrado.setPlaca(placaFormatada);
            }
        }
        if (novoVeiculo.getTipo() != null){
            veiculoRegistrado.setTipo(novoVeiculo.getTipo());
        }
        if (novoVeiculo.getMarca() != null) veiculoRegistrado.setMarca(novoVeiculo.getMarca().trim().toUpperCase());
        if (novoVeiculo.getModelo() != null) veiculoRegistrado.setModelo(novoVeiculo.getModelo().trim().toUpperCase());
        if (novoVeiculo.getCpfProprietario() != null) {
            veiculoRegistrado.setCpfProprietarioHash(hashService.hash(novoVeiculo.getCpfProprietario()));
            veiculoRegistrado.setCpfProprietarioCripto(criptoService.encrypt(novoVeiculo.getCpfProprietario()));
            veiculoRegistrado.setCpfProprietarioLast5(UsuarioUtils.gerarLastN(novoVeiculo.getCpfProprietario(), 5));
            if  ( veiculoRegistrado.getCnpjProprietario() != null ) veiculoRegistrado.setCnpjProprietario(null);
        }
        if (novoVeiculo.getCnpjProprietario() != null){
            veiculoRegistrado.setCnpjProprietario(novoVeiculo.getCnpjProprietario());
            if(veiculoRegistrado.getCpfProprietarioHash() != null){
                veiculoRegistrado.setCpfProprietarioHash(null);
                veiculoRegistrado.setCpfProprietarioCripto(null);
                veiculoRegistrado.setCpfProprietarioLast5(null);
            }
        }
     
        return veiculoRepository.save(veiculoRegistrado);
    }

    public void deleteById(UUID id) {
        Veiculo veiculo = veiculoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Veiculo nao encontrado."));
        UserAuthenticated usuarioLogado = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> authorities = usuarioLogado.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        if(authorities.contains(PermissaoEnum.MOTORISTA.getRole()) || authorities.contains(PermissaoEnum.EMPRESA.getRole())) {
            if(!veiculo.getUsuario().getId().equals(usuarioLogado.id())) {
                throw new IllegalArgumentException("Usuário nao pode deletar veiculo de outro usuário.");
            }
        }
        if (reservaRepository.existsByVeiculoIdAndStatusIn(veiculo.getId(), List.of(StatusReservaEnum.RESERVADA, StatusReservaEnum.ATIVA))) {
            throw new IllegalArgumentException("Veículo não pode ser deletado pois possui reservas ativas ou reservadas.");
        }
        veiculo.setAtivo(false);
        veiculo.setDeletadoEm(DateUtils.agora());
        veiculoRepository.save(veiculo);
    }

    private Veiculo findByIdAtivoAndUsuarioIdAtivo(UUID id, UUID usuarioId) {
        return veiculoRepository.findByIdAndAtivoTrueAndUsuarioIdAndUsuarioAtivoTrue(id, usuarioId).orElseThrow(() -> new VeiculoExceptions.VeiculoNotFoundException());
    }
}