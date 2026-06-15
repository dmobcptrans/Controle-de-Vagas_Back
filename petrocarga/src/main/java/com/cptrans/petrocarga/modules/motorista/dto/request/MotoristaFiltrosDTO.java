package com.cptrans.petrocarga.modules.motorista.dto.request;

public record MotoristaFiltrosDTO(
    String nome,
    String telefone,
    String cnh,
    Boolean ativo
) {}