package com.cptrans.petrocarga.modules.vaga.dto.response;

import java.util.Set;
import java.util.UUID;

import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.response.OperacaoVagaResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class VagaResponseDTO {
    private UUID id;
    private EnderecoVagaResponseDTO endereco;
    private AreaVagaEnum area;
    private String numeroEndereco;
    private String referenciaEndereco;
    private TipoVagaEnum tipoVaga;
    private Double latitudeInicio;
    private Double longitudeInicio;
    private Double latitudeFim;
    private Double longitudeFim;
    private Integer comprimento;
    private Integer quantidade;
    private StatusVagaEnum status;
    private Set<OperacaoVagaResponseDTO> operacoesVaga;
}