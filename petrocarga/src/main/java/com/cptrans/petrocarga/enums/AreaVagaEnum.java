package com.cptrans.petrocarga.enums;

public enum AreaVagaEnum {
    VERMELHA("Vermelha",1),
    AMARELA("Amarela",2),
    AZUL("Azul",4),
    BRANCA("Branca",6);

    private final String descricao;
    private final Integer tempoMaximo;

    private AreaVagaEnum(String descricao, Integer tempoMaximo) {
        this.descricao = descricao;
        this.tempoMaximo = tempoMaximo;
    }

    public String getDescricao() {
        return descricao;
    }
    public Integer getTempoMaximo() {
        return tempoMaximo;
    }
}
