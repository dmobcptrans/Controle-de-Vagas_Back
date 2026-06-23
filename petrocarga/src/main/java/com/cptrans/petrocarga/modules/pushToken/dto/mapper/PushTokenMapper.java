package com.cptrans.petrocarga.modules.pushToken.dto.mapper;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.pushToken.dto.response.PushTokenResponseDTO;
import com.cptrans.petrocarga.modules.pushToken.entity.PushToken;

@Component
public class PushTokenMapper {
    
    public static PushTokenResponseDTO toResponse(PushToken pushToken) {
        return new PushTokenResponseDTO(pushToken.getId(), pushToken.getToken(), pushToken.getPlataforma(), pushToken.isAtivo(), pushToken.getCriadoEm());
    }
}