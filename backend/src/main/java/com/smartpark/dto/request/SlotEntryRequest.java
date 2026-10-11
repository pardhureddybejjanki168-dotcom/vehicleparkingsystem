package com.smartpark.dto.request;

import jakarta.validation.constraints.NotNull;

public class SlotEntryRequest {

    @NotNull(message = "Vehicle selection is required")
    private Long vehicleId;

    @NotNull(message = "Parking slot selection is required")
    private Long slotId;

    public SlotEntryRequest() {
    }

    public SlotEntryRequest(Long vehicleId, Long slotId) {
        this.vehicleId = vehicleId;
        this.slotId = slotId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }
}
