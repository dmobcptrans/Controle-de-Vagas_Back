package com.cptrans.petrocarga.modules.vaga.dto.response;

import java.util.UUID;

import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class VagaSimplificadoResponseDTO {
    private UUID id;
    private UUID enderecoId;
    private String logradouro;
    private String bairro;
    private String numeroEndereco;
    private String referenciaEndereco;
    private AreaVagaEnum area;
    private TipoVagaEnum tipoVaga;
    private Integer comprimento;
    private Integer quantidade;
    private StatusVagaEnum status;
}