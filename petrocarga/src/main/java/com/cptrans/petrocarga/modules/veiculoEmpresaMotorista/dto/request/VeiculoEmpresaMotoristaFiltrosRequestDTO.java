package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.request;

import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoVeiculoEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class VeiculoEmpresaMotoristaFiltrosRequestDTO {
    private UUID veiculoId;
    private String placa;
    private String marca;
    private String modelo;
    private TipoVeiculoEnum tipoVeiculo;
    private Boolean veiculoAtivo;
    private UUID empresaId;
    private String empresaCnpj;
    private String empresaRazaoSocial;
    private UUID motoristaId;
    private String motoristaNome;
    private String motoristaCpf;
    private String motoristaTelefone;
    private String motoristaEmail;
    private Boolean motoristaAtivo;

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public void setMotoristaId(UUID motoristaId) {
        this.motoristaId = motoristaId;
    }

    public void setMotoristaCpf(String motoristaCpf) {
        this.motoristaCpf = motoristaCpf;
    }

    public void setMotoristaTelefone(String motoristaTelefone) {
        this.motoristaTelefone = motoristaTelefone;
    }

    public void setMotoristaEmail(String motoristaEmail) {
        this.motoristaEmail = motoristaEmail;
    }
}