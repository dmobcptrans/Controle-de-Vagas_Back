package com.cptrans.petrocarga.modules.pushToken.dto.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.pushToken.dto.request.PushTokenRequestDTO;
import com.cptrans.petrocarga.modules.pushToken.dto.response.PushTokenResponseDTO;
import com.cptrans.petrocarga.modules.pushToken.entity.PushToken;

@Component
public class PushTokenMapper {

    public PushToken toEntity(PushTokenRequestDTO request, UUID usuarioId) {
        return new PushToken(usuarioId, request.getToken(), request.getPlataforma());
    } 

    public PushTokenResponseDTO toResponse(PushToken pushToken) {
        if (pushToken == null) return null;
        
        PushTokenResponseDTO response = new PushTokenResponseDTO(
            pushToken.getId(), 
            pushToken.getToken(), 
            pushToken.getPlataforma(), 
            pushToken.isAtivo(), 
            pushToken.getCriadoEm()
        );
        response.formatarDados();
        return response;
    }
}