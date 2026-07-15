package com.cptrans.petrocarga.modules.veiculo.dto.request;

import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoVeiculoEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class VeiculoFiltrosRequestDTO {
    private String placa;
    private String marca;
    private String modelo;
    private TipoVeiculoEnum tipo;
    private UUID usuarioId;
    private String telefoneUsuario;
    private String cpfProprietario;
    private String cnpjProprietario; 
    private Boolean ativo;

    public void setTelefoneUsuario(String telefoneUsuario) {
        this.telefoneUsuario = telefoneUsuario;
    }
    
    public void setCpfProprietario(String cpfProprietario) {
        this.cpfProprietario = cpfProprietario;
    }
}