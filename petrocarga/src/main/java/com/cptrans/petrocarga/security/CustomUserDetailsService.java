package com.cptrans.petrocarga.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.usuario.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    private HashService hashService;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


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