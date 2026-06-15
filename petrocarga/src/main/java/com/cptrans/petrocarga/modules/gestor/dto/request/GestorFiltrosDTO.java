package com.cptrans.petrocarga.modules.gestor.dto.request;

public record GestorFiltrosDTO (
    String nome,
    String telefone,
    String email,
    Boolean ativo
) {

}
