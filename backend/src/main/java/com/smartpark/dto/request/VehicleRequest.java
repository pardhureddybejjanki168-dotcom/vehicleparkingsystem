package com.smartpark.dto.request;

import com.smartpark.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VehicleRequest {

    @NotBlank(message = "Vehicle registration number is required")
    @Size(min = 3, max = 25, message = "Registration number must be between 3 and 25 characters")
    private String registrationNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private String makeModel;

    public VehicleRequest() {
    }

    public VehicleRequest(String registrationNumber, VehicleType vehicleType, String makeModel) {
        this.registrationNumber = registrationNumber;
        this.vehicleType = vehicleType;
        this.makeModel = makeModel;
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
}
