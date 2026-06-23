package com.cptrans.petrocarga.modules.usuario.utils;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioUtils {
    public final static String VERSAO_ATUAL_TERMOS = "1.0.0";
    public static String gerarLastN(String string, int n) {
        return string.substring(string.length() - n);
    }
}