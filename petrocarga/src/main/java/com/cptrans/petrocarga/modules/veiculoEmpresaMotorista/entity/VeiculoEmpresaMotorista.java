package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity;

import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "veiculo_empresa_motorista")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class VeiculoEmpresaMotorista {
    @EmbeddedId
    private VeiculoEmpresaMotoristaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("veiculoId")
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("empresaId")
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("motoristaId")
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    public VeiculoEmpresaMotorista(Veiculo veiculo, Empresa empresa, Motorista motorista) {
        this.id = new VeiculoEmpresaMotoristaId(veiculo.getId(), empresa.getId(), motorista.getId());
        this.veiculo = veiculo;
        this.empresa = empresa;
        this.motorista = motorista;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}