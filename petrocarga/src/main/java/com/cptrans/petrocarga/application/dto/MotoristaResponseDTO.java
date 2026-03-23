package com.cptrans.petrocarga.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.enums.TipoCnhEnum;

public class MotoristaResponseDTO {

    private UUID id;
    private UsuarioResponseDTO usuario;
    private TipoCnhEnum tipoCnh;
    private String numeroCnh;
    private LocalDate dataValidadeCnh;
    private UUID empresaId;

    public MotoristaResponseDTO() {
    }

    public MotoristaResponseDTO(Motorista motorista) {
        this.id = motorista.getId();
        this.usuario = new UsuarioResponseDTO(motorista.getUsuario());
        this.tipoCnh = motorista.getTipoCnh();
        this.numeroCnh = motorista.getCnhLast4();
        this.dataValidadeCnh = motorista.getDataValidadeCnh();
        if (motorista.getEmpresa() != null) {
            this.empresaId = motorista.getEmpresa().getId();
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UsuarioResponseDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponseDTO usuario) {
        this.usuario = usuario;
    }

    public TipoCnhEnum getTipoCnh() {
        return tipoCnh;
    }

    public void setTipoCnh(TipoCnhEnum tipoCnh) {
        this.tipoCnh = tipoCnh;
    }

    public String getNumeroCnh() {
        return numeroCnh;
    }

    public void setNumeroCnh(String numeroCnh) {
        this.numeroCnh = numeroCnh;
    }

    public LocalDate getDataValidadeCnh() {
        return dataValidadeCnh;
    }

    public void setDataValidadeCnh(LocalDate dataValidadeCnh) {
        this.dataValidadeCnh = dataValidadeCnh;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }
}
