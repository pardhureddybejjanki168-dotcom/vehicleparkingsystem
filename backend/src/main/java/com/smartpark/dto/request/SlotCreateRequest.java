package com.smartpark.dto.request;

import com.smartpark.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SlotCreateRequest {

    @NotBlank(message = "Slot number is required")
    @Size(min = 2, max = 20, message = "Slot number must be between 2 and 20 characters")
    private String slotNumber;

    @NotNull(message = "Zone ID is required")
    private Long zoneId;

    @NotNull(message = "Vehicle type allowed is required")
    private VehicleType vehicleTypeAllowed = VehicleType.CAR;

    public SlotCreateRequest() {
    }

    public SlotCreateRequest(String slotNumber, Long zoneId, VehicleType vehicleTypeAllowed) {
        this.slotNumber = slotNumber;
        this.zoneId = zoneId;
        this.vehicleTypeAllowed = vehicleTypeAllowed;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public VehicleType getVehicleTypeAllowed() {
        return vehicleTypeAllowed;
    }

    public void setVehicleTypeAllowed(VehicleType vehicleTypeAllowed) {
        this.vehicleTypeAllowed = vehicleTypeAllowed;
    }
}
