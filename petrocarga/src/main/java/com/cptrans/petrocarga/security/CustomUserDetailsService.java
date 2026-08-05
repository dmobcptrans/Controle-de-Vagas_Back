package com.cptrans.petrocarga.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.usuario.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    private final HashService hashService;

    /**
     * Carrega um usuário pelo seu email.
     *
     * @param email O email do usuário a ser carregado.
     * @return O objeto UserDetails do usuário carregado.
     * @throws UsernameNotFoundException se o usuário não for encontrado com o email informado.
    */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailHashAndAtivo(hashService.hash(email), true)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));
    }
}