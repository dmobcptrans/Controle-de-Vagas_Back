package com.cptrans.petrocarga.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.dto.AuthRequestDTO;
import com.cptrans.petrocarga.dto.AuthResponseDTO;
import com.cptrans.petrocarga.dto.CompletarCadastroDTO;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.repositories.UsuarioRepository;
import com.cptrans.petrocarga.security.JwtService;
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
    private MotoristaService motoristaService;

 

    public AuthResponseDTO login(AuthRequestDTO request) {
        if((request.getEmail() == null && request.getCpf() == null) || (request.getEmail() != null && request.getCpf() != null)) throw new IllegalArgumentException("Informe um email OU CPF.");
       
        
        Usuario usuario = usuarioRepository.findByEmailOrCpfHash(request.getEmail(), request.getCpf() == null ? null : cpfHashService.hash(request.getCpf())).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if(usuario.isAtivo().equals(false)) {
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

            Usuario novoUsuario = usuarioService.createMotoristaByGoogleAccount(name, email, googleId);

            String jwt = jwtService.gerarToken(novoUsuario);

            return new AuthResponseDTO(novoUsuario.toResponseDTO(), jwt);
        }

        if (usuario.get().isAtivo().equals(false)) {
            if(usuario.get().getVerificationCode() != null) {
                usuario.get().setVerificationCode(null);
                usuario.get().setVerificationCodeExpiresAt(null);
                usuario.get().setAtivo(true);
                usuarioRepository.save(usuario.get());
            }else{
                throw new IllegalArgumentException("Usuário desativado.");
            }
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
        Usuario usuario = usuarioService.completarCadastro(request, usuarioId);
        if(usuario == null){
            throw new EntityNotFoundException("Usuário não encontrado para completar cadastro.");
        }
        motoristaService.completarCadastro(usuario, request.getNumeroCnh(), request.getDataValidadeCnh(), request.getTipoCnh());
        return usuario;
    }
}
