package com.cptrans.petrocarga.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.models.PushToken;
import com.cptrans.petrocarga.repositories.PushTokenRepository;

@Service
public class PushTokenService {
    @Autowired
    private PushTokenRepository pushTokenRepository;
    
    public PushToken salvar(PushToken novoPushToken){ 
        Optional<PushToken> pushTokenExistente = pushTokenRepository.findByTokenAndUsuarioId(novoPushToken.getToken(), novoPushToken.getUsuarioId());
        
        if (pushTokenExistente.isPresent()) {
            if(!pushTokenExistente.get().getUsuarioId().equals(novoPushToken.getUsuarioId())) throw new IllegalArgumentException("Proibido duplicar token para usuários diferentes.");
            pushTokenExistente.get().setAtivo(true);
            pushTokenExistente.get().setPlataforma(novoPushToken.getPlataforma());
            return pushTokenRepository.save(pushTokenExistente.get());
        } else {
            return pushTokenRepository.save(novoPushToken);
        }

    }

    public List<PushToken> atualizarStatus(UUID usuarioId, boolean ativo) {

        List<PushToken> tokens = pushTokenRepository.findByUsuarioId(usuarioId);

        if(tokens.isEmpty()){
            throw new IllegalArgumentException("Nenhum token encontrado para o usuário");
        }

        tokens.forEach(token -> token.setAtivo(ativo));

        return pushTokenRepository.saveAll(tokens);
    }

    public PushToken visualizarStatus(UUID usuarioId) {
        List<PushToken> push = pushTokenRepository.findByUsuarioId(usuarioId);

        if(push.isEmpty()){
            throw new IllegalArgumentException("Nenhum token encontrado ou vínculado ao usuário");
        }
        return push.getFirst();
    }
}
