package com.cptrans.petrocarga.enums;

public enum PermissaoEnum {
    AGENTE("ROLE_AGENTE"),
    GESTOR("ROLE_GESTOR"),
    MOTORISTA("ROLE_MOTORISTA"),
    EMPRESA("ROLE_EMPRESA"),
    ADMIN("ROLE_ADMIN");

    private final String role;

    PermissaoEnum(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
