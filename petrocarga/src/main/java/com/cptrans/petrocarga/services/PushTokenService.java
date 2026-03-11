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
        Optional<PushToken> pushTokenExistente = pushTokenRepository.findByToken(novoPushToken.getToken());
        
        if (pushTokenExistente.isPresent()) {
            PushToken tokenAtual = pushTokenExistente.get();
            tokenAtual.setUsuarioId(novoPushToken.getUsuarioId());
            tokenAtual.setAtivo(true);
            tokenAtual.setPlataforma(novoPushToken.getPlataforma());
            return pushTokenRepository.save(tokenAtual);
        } else {
            return pushTokenRepository.save(novoPushToken);
        }

    }

    public void desativarPush(String token){
        pushTokenRepository.findByToken(token).ifPresent(pushToken -> {
            pushToken.setAtivo(false);
            pushTokenRepository.save(pushToken);
        });
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
