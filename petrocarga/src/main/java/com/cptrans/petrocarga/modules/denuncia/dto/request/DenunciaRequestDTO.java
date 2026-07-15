package com.cptrans.petrocarga.modules.denuncia.dto.request;

import java.util.UUID;

import com.cptrans.petrocarga.enums.TipoDenunciaEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DenunciaRequestDTO {
    @NotNull
    @NotBlank
    private String descricao;

    @NotNull
    private UUID reservaId;

    @NotNull
    private TipoDenunciaEnum tipo;

    public DenunciaRequestDTO() {
    }
    public DenunciaRequestDTO(String descricao, UUID reservaId, TipoDenunciaEnum tipo) {
        this.descricao = descricao;
        this.reservaId = reservaId;
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }
    public UUID getReservaId() {
        return reservaId;
    }
    public TipoDenunciaEnum getTipo() {
        return tipo;
    }

}
