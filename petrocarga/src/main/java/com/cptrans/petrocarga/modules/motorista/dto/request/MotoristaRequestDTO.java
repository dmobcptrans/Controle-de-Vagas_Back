package com.cptrans.petrocarga.modules.motorista.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoCnhEnum;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MotoristaRequestDTO {

    @NotNull(message = "O campo 'usuario' é obrigatório.")
    private UsuarioRequestDTO usuario;

    @NotNull(message = "O campo 'tipoCnh' é obrigatório.")
    private TipoCnhEnum tipoCnh;

    @NotNull(message = "O campo 'numeroCnh' é obrigatório.")
    @Size(min = 9, max = 11, message = "Número da CNH deve ter entre 9 e 11 caracteres.")
    private String numeroCnh;

    @NotNull(message = "O campo 'dataValidadeCnh' é obrigatório.")
    @Future(message = "Data de validade da CNH está vencida ou vence hoje.")
    private LocalDate dataValidadeCnh;

    private UUID empresaId;

    // public Motorista toEntity(Empresa empresa) {
    //     Motorista motorista = new Motorista();
    //     if (empresa != null){
    //         motorista.setEmpresa(empresa);
    //     }
    //     motorista.setUsuario(this.usuario.toEntity());
    //     motorista.setTipoCnh(this.tipoCnh);
    //     motorista.setCnhHash(this.numeroCnh);
    //     motorista.setDataValidadeCnh(this.dataValidadeCnh);
    //     return motorista;
    // }

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
