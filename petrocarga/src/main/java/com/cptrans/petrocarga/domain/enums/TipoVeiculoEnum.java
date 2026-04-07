package com.cptrans.petrocarga.domain.enums;

public enum TipoVeiculoEnum {
    AUTOMOVEL("Automóvel",5),
    CAMINHONETA("Caminhoneta",6),
    VUC("Veículo Urbano de Carga",8),
    CAMINHAO_MEDIO("Caminhão Médio",12),
    CAMINHAO_LONGO("Caminhão Longo",19);

    private final String descricao;
    private final Integer comprimento;

    private TipoVeiculoEnum(String descricao, Integer comprimento) {
        this.descricao = descricao;
        this.comprimento = comprimento;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getComprimento() {
        return comprimento;
    }
}
