package com.cptrans.petrocarga.modules.pushToken.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.modules.pushToken.dto.mapper.PushTokenMapper;
import com.cptrans.petrocarga.modules.pushToken.dto.request.PushTokenRequestDTO;
import com.cptrans.petrocarga.modules.pushToken.dto.response.PushTokenResponseDTO;
import com.cptrans.petrocarga.modules.pushToken.entity.PushToken;
import com.cptrans.petrocarga.modules.pushToken.exceptions.PushTokenExceptions;
import com.cptrans.petrocarga.modules.pushToken.repository.PushTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushTokenService {
    private final PushTokenRepository pushTokenRepository;
    private final PushTokenMapper pushTokenMapper;

    public PushToken findByTokenAndUsuarioId(String token, UUID usuarioId) {
        return pushTokenRepository.findByTokenAndUsuarioId(token, usuarioId).orElseThrow(() -> new PushTokenExceptions.PushTokenNotFoundException());
    }
    
    public PushTokenResponseDTO registrarToken(PushTokenRequestDTO request, UUID usuarioId) {
        List<PushToken> existentes = pushTokenRepository.findByToken(request.getToken());

        if (existentes == null || existentes.isEmpty()) {
            PushToken novoPushToken = pushTokenRepository.save(pushTokenMapper.toEntity(request, usuarioId));
            return pushTokenMapper.toResponse(novoPushToken);
        }

        PushToken response = null;

        for (PushToken existente : existentes) {
            if (existente.getUsuarioId().equals(usuarioId)) {
                existente.setAtivo(true);
                response = existente;
            } else {
                existente.setAtivo(false);
            }
        }

        pushTokenRepository.saveAll(existentes);
        return pushTokenMapper.toResponse(response);
    }

    public PushToken atualizarStatus(UUID usuarioId, String token, Boolean ativo) {
        PushToken pushToken = findByTokenAndUsuarioId(token, usuarioId);
        pushToken.setAtivo(ativo);
        return pushTokenRepository.save(pushToken);
    }

    public List<PushToken> visualizarStatusByUsuario( UUID usuarioId) {
        return pushTokenRepository.findByUsuarioId(usuarioId);
    }

}