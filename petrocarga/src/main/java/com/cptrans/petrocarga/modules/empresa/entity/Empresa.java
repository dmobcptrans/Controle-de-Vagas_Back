package com.cptrans.petrocarga.modules.empresa.entity;

import java.util.List;
import java.util.UUID;

import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empresa")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class Empresa {
    @Id
    @Column(name = "id")
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name = "id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Motorista> motoristas;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VeiculoEmpresaMotorista> veiculosEmpresaMotoristas;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public List<VeiculoEmpresaMotorista> getVeiculoEmpresaMotoristaAtivos() {
        if (this.veiculosEmpresaMotoristas == null || this.veiculosEmpresaMotoristas.isEmpty()) return null;
        return this.veiculosEmpresaMotoristas.stream().filter((vem) -> {
            return vem.getVeiculo().getAtivo() && vem.getEmpresa().getUsuario().getAtivo() && vem.getMotorista().getUsuario().getAtivo();
        }).toList();
    }
}