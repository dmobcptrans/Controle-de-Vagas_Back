package com.cptrans.petrocarga.modules.vaga.dto.request;

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
public class VagaFiltrosRequestDTO {
    private String codigoPmp;
    private String logradouro;
    private String bairro;
    private AreaVagaEnum area;
    private TipoVagaEnum tipo;
    private StatusVagaEnum status;
}