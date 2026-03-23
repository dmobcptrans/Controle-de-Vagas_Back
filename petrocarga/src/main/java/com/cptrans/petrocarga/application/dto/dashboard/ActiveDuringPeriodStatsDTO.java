package com.cptrans.petrocarga.application.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Métricas calculadas por interseção de intervalo (overlap).
 *
 * <p><b>Diferença importante</b>:</p>
 * <ul>
 *   <li><b>Iniciadas no período</b>: usam {@code inicio BETWEEN startDate AND endDate}.
 *       Ex.: KPIs atuais do dashboard.</li>
 *   <li><b>Ativas no período</b>: consideram reservas que estavam ativas em qualquer momento
 *       dentro do período, mesmo que tenham começado antes de {@code startDate}.
 *       Implementado via interseção: {@code inicio <= endDate AND (fim IS NULL OR fim >= startDate)}.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveDuringPeriodStatsDTO {
    private Integer total;
    private Integer reserva;
    private Integer reservaRapida;
}
