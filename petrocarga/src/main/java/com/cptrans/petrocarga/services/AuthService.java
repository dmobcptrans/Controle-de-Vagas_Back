package com.cptrans.petrocarga.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.dto.AuthRequestDTO;
import com.cptrans.petrocarga.dto.AuthResponseDTO;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.repositories.UsuarioRepository;
import com.cptrans.petrocarga.security.JwtService;

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
}
