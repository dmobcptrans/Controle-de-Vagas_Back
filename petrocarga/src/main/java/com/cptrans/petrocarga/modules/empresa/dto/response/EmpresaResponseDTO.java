package com.cptrans.petrocarga.modules.empresa.dto.response;

import java.util.UUID;

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
    // private List<MotoristaSimplificadoResponseDTO> motoristas;
    // private Map<UUID, List<UUID>> veiculoMotoristas;
}