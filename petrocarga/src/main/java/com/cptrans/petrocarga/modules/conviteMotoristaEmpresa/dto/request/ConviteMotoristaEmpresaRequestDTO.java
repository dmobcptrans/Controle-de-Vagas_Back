package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ConviteMotoristaEmpresaRequestDTO {
    @Email(message = "O email do motorista deve ser um endereço de email válido.")
    @NotBlank(message = "O email do motorista não pode ser nulo ou vazio.")
    private String emailMotorista;

    @NotBlank(message = "O nome do motorista não pode ser nulo ou vazio.")
    private String nomeMotorista;
}