package com.cptrans.petrocarga.application.dto;

public record AgenteFiltrosDTO(
    String nome,
    String telefone,
    String matricula,
    Boolean ativo,
    String email
) {
    
}
