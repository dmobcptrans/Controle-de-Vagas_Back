package com.cptrans.petrocarga.application.dto;

public class VehicleTypeReportDTO {
    private String vehicleType;
    private Long totalCount;
    private Double occupancyPercentage;

    public VehicleTypeReportDTO(String vehicleType, Long totalCount, Double occupancyPercentage) {
        this.vehicleType = vehicleType;
        this.totalCount = totalCount;
        this.occupancyPercentage = occupancyPercentage;
    }

    // Getters and Setters
    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(Double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }
}