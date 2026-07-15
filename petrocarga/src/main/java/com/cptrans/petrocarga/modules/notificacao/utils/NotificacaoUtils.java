package com.cptrans.petrocarga.modules.notificacao.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.notificacao.exceptions.NotificacaoExceptions;

@Component
public class NotificacaoUtils {
    
    /**
     * Valida se um usuário pode enviar uma notificação para outro usuário com base na permissão do usuário autenticado e na permissão do destinatário.
     * Se o usuário autenticado tiver permissão de ADMIN ou GESTOR, ele pode enviar notificações para qualquer outro usuário.
     * Se o usuário autenticado tiver permissão de EMPRESA, MOTORISTA ou AGENTE ele não pode enviar notificações para outros usuários.
     * Se o usuário autenticado tiver uma permissão inválida, lança uma exceção do tipo IllegalArgumentException.
     * 
     * @param permissaoUsuarioLogado a permissão do usuário autenticado
     * @param permissaoDestinatario a permissão do destinatário
     */
    public static void validateByPermissao(PermissaoEnum permissaoUsuarioLogado,  PermissaoEnum permissaoDestinatario) {
        switch (permissaoUsuarioLogado) {
            case ADMIN, GESTOR, AGENTE -> {
                break;
            }
            case EMPRESA -> throw new NotificacaoExceptions.UsuarioNaoPodeEnviarNotificacaoException(PermissaoEnum.EMPRESA);
            case MOTORISTA -> throw new NotificacaoExceptions.UsuarioNaoPodeEnviarNotificacaoException(PermissaoEnum.MOTORISTA);
            default -> throw new AuthExceptions.UsuarioNaoAutorizadoException();
        }
        
    }

    public static void validateByRoles(List<String> roles,  PermissaoEnum permissaoDestinatario) {
        if (roles == null || roles.isEmpty()) {
            throw new AuthExceptions.UsuarioNaoAutenticadoException();
        }
        if (roles.contains(PermissaoEnum.ADMIN.getRole()) || roles.contains(PermissaoEnum.GESTOR.getRole()) || roles.contains(PermissaoEnum.AGENTE.getRole())) {
            return;
        }
        if (roles.contains(PermissaoEnum.EMPRESA.getRole())) {
            throw new NotificacaoExceptions.UsuarioNaoPodeEnviarNotificacaoException(PermissaoEnum.EMPRESA);
        }
        if (roles.contains(PermissaoEnum.MOTORISTA.getRole())) {
            throw new NotificacaoExceptions.UsuarioNaoPodeEnviarNotificacaoException(PermissaoEnum.MOTORISTA);
        }
        throw new AuthExceptions.UsuarioNaoAutorizadoException();
    }
}