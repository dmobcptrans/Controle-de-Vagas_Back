package com.cptrans.petrocarga.modules.pushToken.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.modules.pushToken.entity.PushToken;
import com.cptrans.petrocarga.modules.pushToken.repository.PushTokenRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PushTokenService {
    @Autowired
    private PushTokenRepository pushTokenRepository;
    
    public PushToken salvar(PushToken novoPushToken) {
        List<PushToken> existentes = pushTokenRepository.findByToken(novoPushToken.getToken());

        if (existentes == null || existentes.isEmpty()) {
            novoPushToken.setAtivo(true);
            return pushTokenRepository.save(novoPushToken);
        }

        PushToken response = null;

        for (PushToken existente : existentes) {
            if (existente.getUsuarioId().equals(novoPushToken.getUsuarioId())) {
                existente.setAtivo(true);
                response = existente;
            } else{
                existente.setAtivo(false);
            }
        }

        pushTokenRepository.saveAll(existentes);
        return response;
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
