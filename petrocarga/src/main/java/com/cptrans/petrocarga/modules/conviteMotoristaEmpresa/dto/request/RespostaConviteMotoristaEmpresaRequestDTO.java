package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request;

import com.cptrans.petrocarga.enums.StatusConviteMotoristaEmpresaEnum;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaEmpresaRequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RespostaConviteMotoristaEmpresaRequestDTO {
    @NotBlank(message = "O token do convite não pode ser nulo ou vazio.")
    private String conviteToken;

    @NotNull(message = "O status não pode ser nulo.")
    private StatusConviteMotoristaEmpresaEnum status;

    private MotoristaEmpresaRequestDTO motorista;
}