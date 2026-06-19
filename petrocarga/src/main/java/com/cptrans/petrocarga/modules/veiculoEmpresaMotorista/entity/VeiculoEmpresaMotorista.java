package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity;

import java.util.List;

import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "veiculo_empresa_motorista")
public class VeiculoEmpresaMotorista {

    @EmbeddedId
    private VeiculoEmpresaMotoristaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("veiculoId")
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("motoristaId")
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    // Constructors
    public VeiculoEmpresaMotorista() {
        this.id = new VeiculoEmpresaMotoristaId();
    }

    public VeiculoEmpresaMotorista(Veiculo veiculo, Motorista motorista) {
        this.veiculo = veiculo;
        this.motorista = motorista;
        this.id = new VeiculoEmpresaMotoristaId(veiculo.getId(), motorista.getId());
    }

    // Getters and Setters
    public VeiculoEmpresaMotoristaId getId() {
        return id;
    }

    public void setId(VeiculoEmpresaMotoristaId id) {
        this.id = id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }
}
