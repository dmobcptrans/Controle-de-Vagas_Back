package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request;

import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusConviteMotoristaEmpresaEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RespostaConviteMotoristaExistenteRequestDTO {
    @NotNull(message = "O conviteId não pode ser nulo.")
    private UUID conviteId;

    @NotNull(message = "O status não pode ser nulo.")
    private StatusConviteMotoristaEmpresaEnum status;
}