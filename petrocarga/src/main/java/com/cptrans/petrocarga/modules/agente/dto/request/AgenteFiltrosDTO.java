package com.cptrans.petrocarga.modules.agente.dto.request;

public record AgenteFiltrosDTO(
    String nome,
    String telefone,
    String matricula,
    Boolean ativo,
    String email
) {
    
}
