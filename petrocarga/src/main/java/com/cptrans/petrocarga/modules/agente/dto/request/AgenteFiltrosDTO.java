package com.cptrans.petrocarga.modules.agente.dto.request;

public record AgenteFiltrosDTO(
    String nome,
    String matricula,
    Boolean ativo
) {
}