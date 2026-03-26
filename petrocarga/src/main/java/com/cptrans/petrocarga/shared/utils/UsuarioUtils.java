package com.cptrans.petrocarga.shared.utils;

public class UsuarioUtils {
    public final static String VERSAO_ATUAL_TERMOS = "1.0.0";

    public static String gerarLastN(String string, int n) {
        return string.substring(string.length() - n);
    }
}
