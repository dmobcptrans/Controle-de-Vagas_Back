package com.cptrans.petrocarga.shared.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class StringUtils {
    public static String formatarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) throw new IllegalArgumentException("Campo 'nome' deve ser preenchido.");
        nome = nome.trim();
        String nomeFormatado = " ";
        List<String> preposicoes = List.of("de", "da", "do", "das", "dos");

        for (String n : nome.split(" ")){
            n = n.trim();
            if (!preposicoes.contains(n.toLowerCase()) && n.length() > 0) {
                nomeFormatado += n.substring(0, 1).toUpperCase() + n.substring(1).toLowerCase() + " ";
            }
            else if (!n.isBlank()) {
                nomeFormatado += n.toLowerCase() + " ";
            }
        }

        return nomeFormatado.trim();
    }

    public static String normalize(String input) {
        if (input == null) return null;
        
        return Normalizer.normalize(input, Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}