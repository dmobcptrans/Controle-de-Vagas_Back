package com.cptrans.petrocarga.enums;

public enum TipoDenunciaEnum {
    USO_INDEVIDO_DA_VAGA("Uso indevido da vaga"),
    ATRASO_POR_MOTIVO_DE_FORCA_MAIOR("Atraso por motivo de força maior"),
    OUTROS("Outros");

    private final String descricao;

    private TipoDenunciaEnum(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
