package com.cptrans.petrocarga.modules.veiculo.dto.response;

import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoVeiculoEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class VeiculoResponseDTO {

    private UUID id;
    private String placa;
    private String marca;
    private String modelo;
    private TipoVeiculoEnum tipo;
    private Integer comprimento;
    private UUID usuarioId;
    private String cpfProprietario;
    private String cnpjProprietario;

    public void setCpfProprietario(String cpfProprietario) {
        this.cpfProprietario = cpfProprietario;
    }
}