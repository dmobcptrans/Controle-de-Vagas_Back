package com.cptrans.petrocarga.application.usecase;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.VeiculoRequestDTO;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.entities.Veiculo;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.repositories.ReservaRepository;
import com.cptrans.petrocarga.domain.repositories.VeiculoRepository;
import com.cptrans.petrocarga.infrastructure.security.CriptoService;
import com.cptrans.petrocarga.infrastructure.security.HashService;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.UsuarioUtils;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private HashService cpfHashService;

    @Autowired
    private CriptoService cpfCriptoService;

    @Value("${app.security.aes-criptography.active-key-version}")
    private Integer activeKeyVersion;

    public List<Veiculo> findAll() {
        return veiculoRepository.findAll();
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

    public List<Veiculo> findByUsuarioId(UUID usuarioId) {
        UserAuthenticated usuarioLogado = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioService.findById(usuarioId);
        List<Veiculo> veiculos = veiculoRepository.findByUsuarioAndAtivo(usuario, true);
        
        List<String> authorities = usuarioLogado.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        if(authorities.contains(PermissaoEnum.MOTORISTA.getRole()) || authorities.contains(PermissaoEnum.EMPRESA.getRole())) {
            if(!usuario.getId().equals(usuarioLogado.id())) {
                throw new IllegalArgumentException("Usuário não pode ver os veículos de outro usuário.");
            }
        }

        return veiculos;
    }

    public Veiculo createVeiculo(Veiculo novoVeiculo, UUID usuarioId) {
        Usuario usuarioVeiculo = usuarioService.findById(usuarioId);
        Optional<Veiculo> veiculoByPlaca = veiculoRepository.findByPlacaAndUsuario(novoVeiculo.getPlaca(), usuarioVeiculo);

        UserAuthenticated usuarioLogado = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> authorities = usuarioLogado.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        
        if (!usuarioId.equals(usuarioLogado.id()) && !authorities.contains(PermissaoEnum.ADMIN.getRole())) {
            throw new IllegalArgumentException("Usuário não pode cadastrar veículos de outro usuário.");
        }

        if(veiculoByPlaca.isPresent()){
            if(!veiculoByPlaca.get().isAtivo()){
                veiculoByPlaca.get().setAtivo(true);
                veiculoByPlaca.get().setDeletadoEm(null);
                veiculoByPlaca.get().setUsuario(usuarioVeiculo);
                return veiculoRepository.save(veiculoByPlaca.get());
            }
            throw new IllegalArgumentException("Voce já possui um veículo cadastrado com essa placa.");
        }

        if(novoVeiculo.getCpfProprietarioHash() != null){
            String cpfString= novoVeiculo.getCpfProprietarioHash();
            novoVeiculo.setCpfProprietarioHash(cpfHashService.hash(cpfString));
            novoVeiculo.setCpfProprietarioCripto(cpfCriptoService.encrypt(cpfString));
            novoVeiculo.setCpfProprietarioLast5(UsuarioUtils.gerarLastN(cpfString, 5));
            novoVeiculo.setCpfProprietarioKeyVersion(activeKeyVersion);
        }

        novoVeiculo.setUsuario(usuarioVeiculo);
        return veiculoRepository.save(novoVeiculo);
    }

    public Veiculo updateVeiculo(UUID veiculoId, UUID usuarioId, VeiculoRequestDTO novoVeiculo) {
        Veiculo veiculoRegistrado = findById(veiculoId);
        Usuario usuarioRegistrado = usuarioService.findById(usuarioId);
        UserAuthenticated usuarioLogado = (UserAuthenticated) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> authorities = usuarioLogado.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        
        if (!veiculoRegistrado.getUsuario().getId().equals(usuarioRegistrado.getId()) && (!authorities.contains(PermissaoEnum.ADMIN.getRole()) && !authorities.contains(PermissaoEnum.GESTOR.getRole()))) {
                throw new IllegalArgumentException("Usuário não pode editar veículo de outro usuário.");
        }

        if(novoVeiculo.getCpfProprietario() != null && novoVeiculo.getCnpjProprietario() != null) {
            throw new IllegalArgumentException("Veiculo não pode ter CPF e CNPJ.");
        }

        if (novoVeiculo.getPlaca() != null){
            Optional<Veiculo> veiculoByPlaca = veiculoRepository.findByPlacaAndUsuario(novoVeiculo.getPlaca().toUpperCase(), usuarioRegistrado);
            if(veiculoByPlaca.isPresent() && !veiculoByPlaca.get().getId().equals(veiculoRegistrado.getId())) {
                if(!veiculoByPlaca.get().isAtivo()){
                    veiculoByPlaca.get().setAtivo(true);
                    veiculoByPlaca.get().setDeletadoEm(null);    
                    veiculoRepository.save(veiculoByPlaca.get());
                }
                throw new IllegalArgumentException("Você já possui um veículo cadastrado com essa placa.");
            }else{
                veiculoRegistrado.setPlaca(novoVeiculo.getPlaca().toUpperCase());
            }
        }
        if (novoVeiculo.getTipo() != null){
            veiculoRegistrado.setTipo(novoVeiculo.getTipo());
            veiculoRegistrado.setComprimento(novoVeiculo.getTipo().getComprimento());
        }
        if (novoVeiculo.getMarca() != null) veiculoRegistrado.setMarca(novoVeiculo.getMarca());
        if (novoVeiculo.getModelo() != null) veiculoRegistrado.setModelo(novoVeiculo.getModelo());
        if (novoVeiculo.getCpfProprietario() != null) {
            veiculoRegistrado.setCpfProprietarioHash(cpfHashService.hash(novoVeiculo.getCpfProprietario()));
            veiculoRegistrado.setCpfProprietarioCripto(cpfCriptoService.encrypt(novoVeiculo.getCpfProprietario()));
            veiculoRegistrado.setCpfProprietarioLast5(UsuarioUtils.gerarLastN(novoVeiculo.getCpfProprietario(), 5));
            veiculoRegistrado.setCpfProprietarioKeyVersion(activeKeyVersion);
        }
        if (novoVeiculo.getCnpjProprietario() != null) veiculoRegistrado.setCnpjProprietario(novoVeiculo.getCnpjProprietario());
     
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
        veiculo.setDeletadoEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
        veiculoRepository.save(veiculo);
    }
}
