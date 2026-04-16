package com.cptrans.petrocarga.shared.utils;

import org.springframework.stereotype.Component;

@Component
public class VeiculoUtils {
     private static final String REGEX_PLACA = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$";

    public static String normalizarEValidar(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("Placa não pode ser nula ou vazia.");
        }

        // remove espaços e padroniza
        String placaFormatada = placa.trim().toUpperCase();

        if (placaFormatada.length() != 7) {
            throw new IllegalArgumentException("Placa deve conter 7 caracteres.");
        }   

        if (!placaFormatada.matches(REGEX_PLACA)) {
            throw new IllegalArgumentException("Formato de placa inválido.");
        }

        return placaFormatada;
    }
}
