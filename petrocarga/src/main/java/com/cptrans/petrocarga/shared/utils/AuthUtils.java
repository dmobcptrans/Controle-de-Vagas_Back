package com.cptrans.petrocarga.shared.utils;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;


@Component
public class AuthUtils {
    /**
     * Valida se o usuário autenticado tem permissão para realizar uma ação.
     * A permissão é concedida se o id do usuário autenticado for igual ao id do usuário que precisa de permissão ou se o usuário autenticado tiver alguma das permissões passadas.
     * Caso contrário, é lançada uma exceção do tipo
     * AuthorizationDeniedException.
     *
     * @param userAuthenticated o usuário autenticado
     * @param usuarioId o id do usuário que precisa de permissão
     * @param permissoes a lista de permissões do usuário
     */
    public static void validarPemissoesUsuarioLogado(@AuthenticationPrincipal UserAuthenticated userAuthenticated, UUID usuarioId, List<String> permissoes) {
        if (usuarioId.equals(userAuthenticated.id())) return;
        List<String> authorities = userAuthenticated.userDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        for(String permissao : permissoes){
            if(authorities.contains(permissao)) return;
        }
        throw new AuthorizationDeniedException("Acesso negado.");
    }
}
