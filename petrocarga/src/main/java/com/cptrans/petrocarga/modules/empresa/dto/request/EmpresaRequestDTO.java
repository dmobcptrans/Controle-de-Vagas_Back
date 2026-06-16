package com.cptrans.petrocarga.modules.empresa.dto.request;


import org.hibernate.validator.constraints.br.CNPJ;

import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class EmpresaRequestDTO {

    @NotNull(message = "O campo 'usuario' é obrigatório.")
    private UsuarioRequestDTO usuario;

    @NotNull(message = "O campo 'cnpj' é obrigatório.")
    @CNPJ(message = "Informe um CNPJ válido.")
    private String cnpj;

    @NotNull(message = "O campo 'razaoSocial' é obrigatório.")
    @NotBlank(message = "O campo 'razaoSocial' não pode estar vazio.")
    private String razaoSocial;

    @NotNull
    private Boolean aceitouTermos;

}