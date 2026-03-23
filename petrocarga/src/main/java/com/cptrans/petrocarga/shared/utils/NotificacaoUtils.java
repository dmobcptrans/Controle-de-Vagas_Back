package com.cptrans.petrocarga.shared.utils;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.domain.enums.PermissaoEnum;

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
            case ADMIN, GESTOR -> {
                break;
            }
            case EMPRESA -> throw new IllegalArgumentException("Usuários com permissão EMPRESA não podem enviar notificações para outros usuários.");
            case MOTORISTA -> throw new IllegalArgumentException("Usuários com permissão MOTORISTA não podem enviar notificações para outros usuários.");
            case AGENTE -> throw new IllegalArgumentException("Usuários com permissão AGENTE não podem enviar notificações para outros usuários.");
            default -> throw new IllegalArgumentException("Permissão de usuário inválida.");
        }
        
    }
}

