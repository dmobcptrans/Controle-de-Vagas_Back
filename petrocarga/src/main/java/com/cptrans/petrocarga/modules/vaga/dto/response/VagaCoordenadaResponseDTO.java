package com.cptrans.petrocarga.modules.vaga.dto.response;

import java.util.UUID;

import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class VagaCoordenadaResponseDTO {
    private UUID id;
    private AreaVagaEnum area;
    private StatusVagaEnum status;
    private Double latitudeInicio;
    private Double longitudeInicio;
    private Double latitudeFim;
    private Double longitudeFim;
}