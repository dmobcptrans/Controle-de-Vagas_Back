package com.cptrans.petrocarga.utils;

public class UsuarioUtils {
    public final static String VERSAO_ATUAL_TERMOS = "1.0.0";

    public static String gerarLast5(String cpf) {
        return cpf.substring(cpf.length() - 5);
    }
}
