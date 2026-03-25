package com.cptrans.petrocarga.domain.entities;

import java.time.LocalDate;
import java.util.UUID;

import com.cptrans.petrocarga.application.dto.MotoristaResponseDTO;
import com.cptrans.petrocarga.domain.enums.TipoCnhEnum;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "motorista")
public class Motorista {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cnh", length=2, nullable = false)
    private TipoCnhEnum tipoCnh;

    @Column(name = "cnh_hash", unique = true, nullable = false)
    private String cnhHash;

    @Column(name = "cnh_cripto", unique = true, nullable = false)
    private String cnhCripto;

    @Column(name = "cnh_last4", nullable = false)
    private String cnhLast4;

    @Column(name = "data_validade_cnh")
    private LocalDate dataValidadeCnh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    // Constructors
    public Motorista() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoCnhEnum getTipoCnh() {
        return tipoCnh;
    }

    public void setTipoCnh(TipoCnhEnum tipoCnh) {
        this.tipoCnh = tipoCnh;
    }

    public String getCnhHash() {
        return cnhHash;
    }

    public void setCnhHash(String cnhHash) {
        this.cnhHash = cnhHash;
    }

    public String getCnhCripto() {
        return cnhCripto;
    }

    public void setCnhCripto(String cnhCripto) {
        this.cnhCripto = cnhCripto;
    }

    public String getCnhLast4() {
        return cnhLast4;
    }

    public void setCnhLast4(String cnhLast4) {
        this.cnhLast4 = cnhLast4;
    }

    public LocalDate getDataValidadeCnh() {
        return dataValidadeCnh;
    }

    public void setDataValidadeCnh(LocalDate dataValidadeCnh) {
        this.dataValidadeCnh = dataValidadeCnh;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public MotoristaResponseDTO toResponseDTO() {
        return new MotoristaResponseDTO(this);
    }
}
