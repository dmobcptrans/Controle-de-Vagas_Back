package com.cptrans.petrocarga.modules.vaga.dto.response;

import java.util.List;

import com.cptrans.petrocarga.enums.TipoMapaVagasEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter 
public class VagasMapaResponseDTO {
    private TipoMapaVagasEnum tipo;
    private List<VagaCoordenadaResponseDTO> vagas;
    private List<VagasClusterResponseDTO> clusters;
    private boolean limiteAtingido;
}