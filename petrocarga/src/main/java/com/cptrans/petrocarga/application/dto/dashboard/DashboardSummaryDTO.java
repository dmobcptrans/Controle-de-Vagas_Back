package com.cptrans.petrocarga.application.dto.dashboard;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private DashboardKpiDTO kpis;
    private List<VehicleTypeStatDTO> vehicleTypes;
    private List<LocationStatDTO> districts;
    private List<LocationStatDTO> origins;
    private List<LocationStatDTO> entryOrigins;
    private List<LocationStatDTO> mostUsedVagas;
    private StayDurationStatsDTO stayDurationStats;
    private ActiveDuringPeriodStatsDTO activeDuringPeriodStats;
    private LengthOccupancyStatsDTO lengthOccupancyStats;
    private List<VehicleRouteReportDTO> vehicleRoutes;
}