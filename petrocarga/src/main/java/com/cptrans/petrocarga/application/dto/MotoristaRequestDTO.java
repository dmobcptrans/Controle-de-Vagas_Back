package com.cptrans.petrocarga.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Empresa;
import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.enums.TipoCnhEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MotoristaRequestDTO {

    @NotNull(message = "O campo 'usuario' é obrigatório.")
    @Valid
    private UsuarioRequestDTO usuario;

    @NotNull(message = "O campo 'tipoCnh' é obrigatório.")
    @Valid
    private TipoCnhEnum tipoCnh;

    @NotNull(message = "O campo 'numeroCnh' é obrigatório.")
    @Size(min = 9, max = 11, message = "Número da CNH deve ter entre 9 e 11 caracteres.")
    private String numeroCnh;

    @NotNull(message = "O campo 'dataValidadeCnh' é obrigatório.")
    @Valid
    @Future(message = "Data de validade da CNH deve ser futura.")
    private LocalDate dataValidadeCnh;

    @Valid
    private UUID empresaId;

    public Motorista toEntity(Empresa empresa) {
        Motorista motorista = new Motorista();
        if (empresa != null){
            motorista.setEmpresa(empresa);
        }
        motorista.setUsuario(this.usuario.toEntity());
        motorista.setTipoCnh(this.tipoCnh);
        motorista.setCnhHash(this.numeroCnh);
        motorista.setDataValidadeCnh(this.dataValidadeCnh);
        return motorista;
    }

    // Getters and Setters
    public UsuarioRequestDTO getUsuario() {
        return usuario;
    }
    public TipoCnhEnum getTipoCnh() {
        return tipoCnh;
    }
    public String getNumeroCnh() {
        return numeroCnh;
    }
    public LocalDate getDataValidadeCnh() {
        return dataValidadeCnh;
    }
    public UUID getEmpresaId() {
        return empresaId;
    }
}
