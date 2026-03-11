package com.cptrans.petrocarga.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.models.PushToken;
import com.cptrans.petrocarga.repositories.PushTokenRepository;

import jakarta.persistence.EntityNotFoundException;

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

    public PushToken atualizarStatus(UUID usuarioId, String token, Boolean ativo) {
        PushToken pushToken = pushTokenRepository.findByTokenAndUsuarioId(token, usuarioId).orElseThrow(() -> new EntityNotFoundException("Nenhum token encontrado para o usuário"));
        
        pushToken.setAtivo(ativo);

        return pushTokenRepository.save(pushToken);
    }

    public PushToken visualizarStatusByTokenAndUsuario(String token, UUID usuarioId) {
        Optional<PushToken> push = pushTokenRepository.findByTokenAndUsuarioId(token, usuarioId);

        if(push.isEmpty()){
            throw new EntityNotFoundException("Nenhum token encontrado ou vínculado ao usuário");
        }

        return push.get();
    }

    public List<PushToken> visualizarStatusByUsuario( UUID usuarioId) {
        List<PushToken> pushList = pushTokenRepository.findByUsuarioId(usuarioId);

        if(pushList.isEmpty()){
            throw new EntityNotFoundException("Nenhum token encontrado ou vínculado ao usuário");
        }

        return pushList;
    }

}
