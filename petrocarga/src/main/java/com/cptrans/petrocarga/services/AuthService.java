package com.cptrans.petrocarga.services;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.dto.AuthRequestDTO;
import com.cptrans.petrocarga.dto.AuthResponseDTO;
import com.cptrans.petrocarga.dto.CompletarCadastroDTO;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.models.Motorista;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.repositories.MotoristaRepository;
import com.cptrans.petrocarga.repositories.UsuarioRepository;
import com.cptrans.petrocarga.security.JwtService;
import com.cptrans.petrocarga.utils.DateUtils;
import com.cptrans.petrocarga.utils.UsuarioUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AuthService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CpfHashService cpfHashService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CpfCriptoService cpfCriptoService;

    @Autowired
    private MotoristaRepository motoristaRepository;


    @Value("${app.security.cpf.active-key-version:1}")
    private Integer cpfActiveKeyVersion;

    public AuthResponseDTO login(AuthRequestDTO request) {
        if((request.getEmail() == null && request.getCpf() == null) || (request.getEmail() != null && request.getCpf() != null)) throw new IllegalArgumentException("Informe um email OU CPF.");
       
        
        Usuario usuario = usuarioRepository.findByEmailOrCpfHash(request.getEmail(), request.getCpf() == null ? null : cpfHashService.hash(request.getCpf())).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if(usuario.getAtivo().equals(false)) {
            throw new IllegalArgumentException("Usuário desativado.");
        }
        if(!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }
        String token = jwtService.gerarToken(usuario);

       return new AuthResponseDTO(usuario.toResponseDTO(), token);
    }

    public AuthResponseDTO loginWithGoogle(String token)  {

        Payload payload = googleAuthService.verifyGoogleToken(token);

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");

        Optional<Usuario> usuario = usuarioRepository.findByEmailOrGoogleId(email, googleId);

        if (!usuario.isPresent()) {

            Usuario novoUsuario = usuarioService.createUsuarioByGoogleAccount(name, email, googleId);

            String jwt = jwtService.gerarToken(novoUsuario);

            return new AuthResponseDTO(novoUsuario.toResponseDTO(), jwt);
        }

        if (usuario.get().getAtivo().equals(false)) {
            throw new IllegalArgumentException("Usuário desativado.");
        }

        if(usuario.isPresent() && usuario.get().getGoogleId() == null) {
            usuario.get().setGoogleId(googleId);
            usuario.get().setProvider(UsuarioProviderEnum.GOOGLE);
            usuarioRepository.save(usuario.get());
        }

        String jwt = jwtService.gerarToken(usuario.get());

        return new AuthResponseDTO(usuario.get().toResponseDTO(), jwt);
    }

    @Transactional
    public Usuario completarCadastro(CompletarCadastroDTO request, UUID usuarioId){
        if(request.getAceitarTermos().equals(Boolean.FALSE)) throw new IllegalArgumentException("Usuário não aceitou os termmos de uso e privacidade dos dados.");

        Usuario usuarioCadastrado = usuarioRepository.findByIdAndAtivo(usuarioId, true).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado ou desativado"));
        
        String cpfHash = cpfHashService.hash(request.getCpf());
        Optional<Usuario> usuarioByCpf = usuarioRepository.findByCpfHash(cpfHash);

        if (usuarioByCpf.isPresent() && !usuarioByCpf.get().getId().equals(usuarioCadastrado.getId())) throw new IllegalArgumentException("CPF já cadastrado.");

        usuarioCadastrado.setCpfHash(cpfHash);
        usuarioCadastrado.setCpfCripto(cpfCriptoService.encrypt(request.getCpf()));
        usuarioCadastrado.setAceitarTermos(request.getAceitarTermos());
        usuarioCadastrado.setAceitouTermosEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
        usuarioCadastrado.setCpfKeyVersion(cpfActiveKeyVersion);
        usuarioCadastrado.setVersaoTermos(UsuarioUtils.VERSAO_ATUAL_TERMOS);
        usuarioCadastrado.setTelefone(request.getTelefone());
        usuarioCadastrado.setSenha(request.getSenha() != null ? request.getSenha() : null);

        Usuario usuarioAtualizado = usuarioRepository.save(usuarioCadastrado);

        Optional<Motorista> motorista = motoristaRepository.findByUsuarioId(usuarioId);
        
        if(motorista.isPresent()){
            Optional<Motorista> motoristaByCnh = motoristaRepository.findByNumeroCnh(request.getNumeroCnh());
            if(motoristaByCnh.isPresent() && !motoristaByCnh.get().getUsuario().getId().equals(usuarioAtualizado.getId())){
                throw new IllegalArgumentException("CNH já cadastrada");
            }
            else{
                motorista.get().setDataValidadeCnh(request.getDataValidadeCnh());
                motorista.get().setNumeroCnh(request.getNumeroCnh());
                motorista.get().setTipoCnh(request.getTipoCnh());
                motoristaRepository.save(motorista.get());
            }
        }else{
            Motorista novoMotorista = new Motorista();
            novoMotorista.setDataValidadeCnh(request.getDataValidadeCnh());
            novoMotorista.setNumeroCnh(request.getNumeroCnh());
            novoMotorista.setTipoCnh(request.getTipoCnh());
            novoMotorista.setUsuario(usuarioAtualizado);
            motoristaRepository.save(novoMotorista);
        }
        return usuarioAtualizado;
    }
}
