package com.cptrans.petrocarga.enums;

public enum UsuarioProviderEnum {
    GOOGLE("Google"),
    LOCAL("Local");

    private final String descricao;

    private UsuarioProviderEnum(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
        return this.descricao;
    }

}
