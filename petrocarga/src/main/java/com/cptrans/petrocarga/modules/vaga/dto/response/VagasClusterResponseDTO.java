package com.cptrans.petrocarga.modules.vaga.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor 
@Getter 
public class VagasClusterResponseDTO {
    private Double latitude;
    private Double longitude;
    private Long quantidade;
}