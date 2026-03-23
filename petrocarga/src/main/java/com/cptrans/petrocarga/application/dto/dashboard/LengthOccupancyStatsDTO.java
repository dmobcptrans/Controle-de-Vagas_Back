package com.cptrans.petrocarga.application.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ocupação por comprimento (m) ao longo de um período.
 *
 * <p>Para evitar distorções quando a reserva começa antes do período ou termina depois,
 * o cálculo de "ocupado" é ponderado pelo tempo de interseção (overlap) com o intervalo
 * {@code [startDate, endDate]}.</p>
 *
 * <ul>
 *   <li>{@code availableLengthMeters}: soma do comprimento (m) de todas as vagas.</li>
 *   <li>{@code occupiedLengthMeters}: comprimento médio ocupado (m) durante o período
 *       (derivado de somatório de (comprimento do veículo × minutos de overlap) / minutos do período).</li>
 *   <li>{@code occupancyRatePercent}: {@code occupiedLengthMeters / availableLengthMeters * 100}.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LengthOccupancyStatsDTO {
    private Double availableLengthMeters;
    private Double occupiedLengthMeters;
    private Double occupancyRatePercent;
}
