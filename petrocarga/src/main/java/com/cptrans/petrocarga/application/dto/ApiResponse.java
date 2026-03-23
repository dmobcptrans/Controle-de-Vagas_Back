package com.cptrans.petrocarga.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO genérico para respostas da API.
 * Fornece mensagens claras para o frontend exibir ao usuário.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(
    boolean success,
    String message,
    String code,
    Object data
) {
    // Construtores de conveniência
    
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null, null);
    }
    
    public static ApiResponse success(String message, String code) {
        return new ApiResponse(true, message, code, null);
    }
    
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, null, data);
    }
    
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null, null);
    }
    
    public static ApiResponse error(String message, String code) {
        return new ApiResponse(false, message, code, null);
    }
}
