package com.cptrans.petrocarga.shared.utils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

@Component
public class DateUtils {
    public static final ZoneId FUSO_BRASIL = ZoneOffset.of("-03:00");
    /**
     * Converte um OffsetDateTime para um LocalDate no fuso horário do Brasil (Brasília).
     * Se o OffsetDateTime for nulo, retorna nulo.
     * 
     * @param data o OffsetDateTime a ser convertido
     * @return o LocalDate convertido ou nulo se o OffsetDateTime for nulo
     */
    public static LocalDate toLocalDateInBrazil(OffsetDateTime data) {
        if (data == null) return null;
        return data.atZoneSameInstant(ZoneOffset.of("-03:00")).toLocalDate();
    }
}
