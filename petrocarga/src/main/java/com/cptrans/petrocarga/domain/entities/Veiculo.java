package com.cptrans.petrocarga.domain.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.application.dto.VeiculoResponseDTO;
import com.cptrans.petrocarga.domain.enums.TipoVeiculoEnum;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "veiculo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"placa", "usuario_id"})
})
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

    @Column(name = "cpf_proprietario_key_version")
    private Integer cpfProprietarioKeyVersion;

    @Column(name = "cpf_proprietario_last5")
    private String cpfProprietarioLast5;

    @Column(name = "cnpj_proprietario", length = 14)
    private String cnpjProprietario;

    @Column(name = "ativo", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean ativo;

    @Column(name = "deletado_em", nullable = true)
    private OffsetDateTime deletadoEm;

    // Constructors
    public Veiculo() {
        this.ativo = true;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public TipoVeiculoEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoVeiculoEnum tipo) {
        this.tipo = tipo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getCpfProprietarioHash() {
        return cpfProprietarioHash;
    }

    public void setCpfProprietarioHash(String cpfProprietarioHash) {
        this.cpfProprietarioHash = cpfProprietarioHash;
    }

     public String getCpfProprietarioCripto() {
        return cpfProprietarioCripto;
    }

    public void setCpfProprietarioCripto(String cpfProprietarioCripto) {
        this.cpfProprietarioCripto = cpfProprietarioCripto;
    }

    public Integer getCpfProprietarioKeyVersion() {
        return cpfProprietarioKeyVersion;
    }

    public void setCpfProprietarioKeyVersion(Integer cpfProprietarioKeyVersion) {
        this.cpfProprietarioKeyVersion = cpfProprietarioKeyVersion;
    }

    public String getCpfProprietarioLast5() {
        return cpfProprietarioLast5;
    }

    public void setCpfProprietarioLast5(String cpfProprietarioLast5) {
        this.cpfProprietarioLast5 = cpfProprietarioLast5;
    }

    public String getCnpjProprietario() {
        return cnpjProprietario;
    }

    public void setCnpjProprietario(String cnpjProprietario) {
        this.cnpjProprietario = cnpjProprietario;
    }

    public Boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public OffsetDateTime getDeletadoEm() {
        return deletadoEm;
    }

    public void setDeletadoEm(OffsetDateTime deletadoEm) {
        this.deletadoEm = deletadoEm;
    }

    public VeiculoResponseDTO toResponseDTO() {
        return new VeiculoResponseDTO(this);
    }
}