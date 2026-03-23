package com.cptrans.petrocarga.domain.enums;

public enum DiaSemanaEnum {
    DOMINGO (1, "Domingo"),
    SEGUNDA (2, "Segunda-feira"),
    TERCA (3, "Terça-feira"),
    QUARTA (4, "Quarta-feira"),
    QUINTA (5, "Quinta-feira"),
    SEXTA (6, "Sexta-feira"),
    SABADO (7, "Sábado");

    public Integer codigo;
    public String descricao;

    private DiaSemanaEnum(Integer codigo, String descricao){
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static DiaSemanaEnum toEnumByCodigo(Integer codigo){
        if(codigo == null || codigo < 1 || codigo > 7){
            throw new IllegalArgumentException("Dia da semana inválido. Código não pode ser nulo ou menor que 1 ou maior que 7.");
        }

        for(DiaSemanaEnum dia : DiaSemanaEnum.values()){
            if(codigo.equals(dia.codigo)){
                return dia;
            }
        }

        throw new IllegalArgumentException("Dia da semana inválido. Código: " + codigo);
    }
    
    public static DiaSemanaEnum toEnumByDescricao(String descricao){
        if(descricao == null || descricao.isEmpty()){
            return null;
        }

        for(DiaSemanaEnum dia : DiaSemanaEnum.values()){
            if(descricao.equals(dia.descricao)){
                return dia;
            }
        }

        throw new IllegalArgumentException("Dia da semana inválido. Descricao Enviada: " + descricao);
    }

    public Integer getCodigo(){
        return this.codigo;
    }
    public String getDescricao(){
        return this.descricao;
    }
}
