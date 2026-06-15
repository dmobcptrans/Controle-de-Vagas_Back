package com.cptrans.petrocarga.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StayDurationStatsDTO {
    private Double avgMinutes;
    private Double minMinutes;
    private Double maxMinutes;
}
