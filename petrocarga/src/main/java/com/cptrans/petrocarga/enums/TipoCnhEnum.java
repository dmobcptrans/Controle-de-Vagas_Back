package com.cptrans.petrocarga.enums;

public enum TipoCnhEnum {
    AB("Autómvel e Moto"),
    B("Autómvel"),
    C("Carga"),
    AC("Autómvel e Carga"),
    D("Ônibus"),
    AD("Ônibus e Moto"),
    E("Articulados"),
    AE("Articulados e Moto");

    private final String descricao;

    private TipoCnhEnum(String descricao) {
        this.descricao = descricao;
    }
    public String getDescricao() {
        return descricao;
    }
}