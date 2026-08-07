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

    public static String removerAcentos(String input) {
        if (input == null) return null;
        
        return Normalizer.normalize(input, Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public static String aplicarMascaraCpf(String cpfLast5){
        if (cpfLast5 == null || cpfLast5.trim().length() != 5) return null;
        cpfLast5 = cpfLast5.trim();
        return "***.***." + cpfLast5.substring(0,3) + "-" + cpfLast5.substring(3);
    }

    public static String formatarCnpj(String cnpj){
        if (cnpj == null || cnpj.trim().length() != 14) return null;
        cnpj = cnpj.trim();
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12, 14);
    }

    public static String formatarCpf(String cpf){
        if (cpf == null || cpf.trim().length() != 11) return null;
        cpf = cpf.trim();
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }

    public static String formatarTelefone(String telefone) {
        if (telefone == null || telefone.trim().length() < 10 || telefone.trim().length() > 11) return null;
        telefone = telefone.trim();
        String ddd = telefone.substring(0, 2);
        String parte1 = telefone.length() == 10 ? telefone.substring(2, 6) : telefone.substring(2, 7);
        String parte2 = telefone.length() == 10 ? telefone.substring(6) : telefone.substring(7);
        return "(" + ddd + ") " + parte1 + "-" + parte2;
    }
}