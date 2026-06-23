package com.cptrans.petrocarga.shared.utils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

@Component
public class DateUtils {
    public static final ZoneId FUSO_BRASILIA = ZoneOffset.of("-03:00");
    /**
     * Converte um OffsetDateTime para um LocalDate no fuso horário do Brasil (Brasília).
     * Se o OffsetDateTime for nulo, retorna nulo.
     * 
     * @param data o OffsetDateTime a ser convertido
     * @return o LocalDate convertido ou nulo se o OffsetDateTime for nulo
     */
    public static LocalDate toLocalDateInBrazil(OffsetDateTime data) {
        if (data == null) return null;
        return data.atZoneSameInstant(ZoneOffset.of(FUSO_BRASILIA.toString())).toLocalDate();
    }

    public static void validarMesEAno(Integer mes, Integer ano) {
        if(mes != null && (mes < 1 || mes > 12)) {
            throw new IllegalArgumentException("Mês deve ser um valor entre 1 e 12.");
        }

        if (ano != null && (ano < 2026 || ano > 2100)) {
            throw new IllegalArgumentException("Ano deve ser um valor entre 2026 e 2100.");
        }
    }

    public static OffsetDateTime fusoHorarioBrasilia(OffsetDateTime data) {
        return data.atZoneSameInstant(FUSO_BRASILIA).toOffsetDateTime();
    }

    public static OffsetDateTime agora(){
        return OffsetDateTime.now(FUSO_BRASILIA);
    }

    public static OffsetDateTime getInicioMes(int mes, int ano) {
        return OffsetDateTime.of(ano, mes, 1, 0, 0, 0, 0, ZoneOffset.of(FUSO_BRASILIA.toString()));
    }

    public static OffsetDateTime getFimMes(Integer mes, Integer ano) {
        Boolean anoTerminaComZeroZero = ano.toString().endsWith("00"); 
        Boolean anoBissexto = ((ano % 4 == 0) || (anoTerminaComZeroZero && ano % 400 == 0)); 
        Integer ultimoDiaMes = getInicioMes(mes, ano).getMonth().length(anoBissexto);
        return OffsetDateTime.of(ano, mes, ultimoDiaMes, 23, 59, 59, 0, ZoneOffset.of(FUSO_BRASILIA.toString()));
    }
}