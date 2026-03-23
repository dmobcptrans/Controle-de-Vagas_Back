package com.cptrans.petrocarga.domain.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class VeiculoEmpresaMotoristaId implements Serializable {

    @Column(name = "veiculo_id")
    private UUID veiculoId;

    @Column(name = "motorista_id")
    private UUID motoristaId;

    public VeiculoEmpresaMotoristaId() {}

    public VeiculoEmpresaMotoristaId(UUID veiculoId, UUID motoristaId) {
        this.veiculoId = veiculoId;
        this.motoristaId = motoristaId;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(UUID veiculoId) {
        this.veiculoId = veiculoId;
    }

    public UUID getMotoristaId() {
        return motoristaId;
    }

    public void setMotoristaId(UUID motoristaId) {
        this.motoristaId = motoristaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VeiculoEmpresaMotoristaId that = (VeiculoEmpresaMotoristaId) o;
        return Objects.equals(veiculoId, that.veiculoId) &&
               Objects.equals(motoristaId, that.motoristaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(veiculoId, motoristaId);
    }
}
