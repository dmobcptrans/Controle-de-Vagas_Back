package com.cptrans.petrocarga.modules.veiculo.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Table(name = "veiculo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"placa", "usuario_id"})
})
@Getter
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false, length = 7, columnDefinition="CHAR(7)")
    private String placa;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Enumerated(EnumType.STRING)
    private TipoVeiculoEnum tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "cpf_proprietario_hash")
    private String cpfProprietarioHash;

    @Column(name = "cpf_proprietario_cripto")
    private String cpfProprietarioCripto;

    @Column(name = "cpf_proprietario_last5")
    private String cpfProprietarioLast5;

    @Column(name = "cnpj_proprietario", length = 14)
    private String cnpjProprietario;

    @Column(name = "ativo", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean ativo = true;

    @Column(name = "deletado_em", nullable = true)
    private OffsetDateTime deletadoEm;

    @OneToMany(mappedBy = "veiculo", fetch = FetchType.LAZY)
    private List<VeiculoEmpresaMotorista> veiculosEmpresaMotoristas;

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setTipo(TipoVeiculoEnum tipo) {
        this.tipo = tipo;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setCpfProprietarioHash(String cpfProprietarioHash) {
        this.cpfProprietarioHash = cpfProprietarioHash;
    }

    public void setCpfProprietarioCripto(String cpfProprietarioCripto) {
        this.cpfProprietarioCripto = cpfProprietarioCripto;
    }

    public void setCpfProprietarioLast5(String cpfProprietarioLast5) {
        this.cpfProprietarioLast5 = cpfProprietarioLast5;
    }

    public void setCnpjProprietario(String cnpjProprietario) {
        this.cnpjProprietario = cnpjProprietario;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public void setDeletadoEm(OffsetDateTime deletadoEm) {
        this.deletadoEm = deletadoEm;
    }

    public List<Motorista> getMotoristasByVeiculoId(UUID veiculoId){
        if(this.veiculosEmpresaMotoristas == null || this.veiculosEmpresaMotoristas.isEmpty()) return List.of();
        List<Motorista> motoristas = new ArrayList<>();
        for (VeiculoEmpresaMotorista vem : this.veiculosEmpresaMotoristas) {
            if (vem.getVeiculo().getId().equals(veiculoId)){
                motoristas.add(vem.getMotorista());
            }
        }
        return motoristas;
    }

}