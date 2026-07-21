package com.cptrans.petrocarga.modules.veiculo.utils;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.veiculo.exceptions.VeiculoExceptions;

@Component
public class VeiculoUtils {
     private static final String REGEX_PLACA = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$";

    public static String normalizarEValidar(String placa) {
        if (placa == null || placa.isBlank()) throw new VeiculoExceptions.PlacaInvalidaExceptions();

        String placaFormatada = placa.trim().toUpperCase();

        if (placaFormatada.length() != 7) throw new VeiculoExceptions.PlacaInvalidaExceptions();

        if (!placaFormatada.matches(REGEX_PLACA)) throw new VeiculoExceptions.PlacaInvalidaExceptions();

        return placaFormatada;
    }
}
