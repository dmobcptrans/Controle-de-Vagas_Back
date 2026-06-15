package com.cptrans.petrocarga.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTypeStatDTO {
    private String type;
    private Integer count;
    private Integer uniqueVehicles;
}
