package com.cptrans.petrocarga.shared.dto.response;


/**
 * DTO genérico para respostas da API.
 * Fornece mensagens claras para o frontend exibir ao usuário.
 */
public record SystemResponse(
    String message,
    Integer code
) {
}