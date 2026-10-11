package com.smartpark.dto.response;

import com.smartpark.entity.Vehicle;
import com.smartpark.entity.VehicleType;
import java.time.LocalDateTime;

public class VehicleResponse {

    private Long id;
    private String registrationNumber;
    private VehicleType vehicleType;
    private String makeModel;
    private LocalDateTime createdAt;

    public VehicleResponse() {
    }

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setRegistrationNumber(vehicle.getRegistrationNumber());
        response.setVehicleType(vehicle.getVehicleType());
        response.setMakeModel(vehicle.getMakeModel());
        response.setCreatedAt(vehicle.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getMakeModel() {
        return makeModel;
    }

    public void setMakeModel(String makeModel) {
        this.makeModel = makeModel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
