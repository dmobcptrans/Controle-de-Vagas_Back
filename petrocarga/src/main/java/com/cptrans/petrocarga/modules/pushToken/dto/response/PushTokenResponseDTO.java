package com.cptrans.petrocarga.modules.pushToken.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.PlataformaEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PushTokenResponseDTO {
    private UUID usuarioId;
    private String token;
    private PlataformaEnum plataforma;
    private boolean ativo;
    private OffsetDateTime criadoEm;
}