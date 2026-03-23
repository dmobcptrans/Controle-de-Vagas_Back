package com.cptrans.petrocarga.application.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Relatório de trajeto reconstruído a partir de Reserva/ReservaRapida.
 *
 * <p>A lista de paradas é ordenada temporalmente por {@code inicio}.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRouteReportDTO {
    private String placa;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime periodStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime periodEnd;
    private List<VehicleRouteStopDTO> stops;
}
