package com.cptrans.petrocarga.modules.enderecoVaga.dto.response;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class EnderecoVagaResponseDTO {
    private UUID id;
    private String codigoPmp;
    private String logradouro;  
    private String bairro;
}