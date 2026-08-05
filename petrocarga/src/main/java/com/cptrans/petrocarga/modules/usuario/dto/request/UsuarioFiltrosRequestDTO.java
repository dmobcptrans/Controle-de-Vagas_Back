package com.cptrans.petrocarga.modules.usuario.dto.request;

import java.util.List;

import com.cptrans.petrocarga.enums.PermissaoEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class UsuarioFiltrosRequestDTO {
    private String nome;
    private String email;
    private String telefone;
    private List<PermissaoEnum> listaPermissoes;
    private Boolean ativo;
}