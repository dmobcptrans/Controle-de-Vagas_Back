package com.cptrans.petrocarga.modules.empresa.dto.response;

import java.util.List;
import java.util.UUID;

import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmpresaResponseDTO {

    private UUID id;
    private UsuarioResponseDTO usuario;
    private String cnpj;
    private String razaoSocial;
    private List<MotoristaSimplificadoResponseDTO> motoristas;

}