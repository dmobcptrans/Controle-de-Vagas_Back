package com.cptrans.petrocarga.application.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiDTO {
    private Integer totalSlots;
    private Integer activeReservations;
    private Integer pendingReservations;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.0")
    private Double occupancyRate;

    private Integer completedReservations;
    private Integer canceledReservations;
    private Integer removedReservations;
    private Integer totalReservations;
    private Integer multipleSlotReservations;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime endDate;
}
