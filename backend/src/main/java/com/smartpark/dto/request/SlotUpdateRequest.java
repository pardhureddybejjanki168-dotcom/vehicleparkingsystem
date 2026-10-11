package com.smartpark.dto.request;

import com.smartpark.entity.SlotStatus;
import com.smartpark.entity.VehicleType;

public class SlotUpdateRequest {

    private SlotStatus status;
    private VehicleType vehicleTypeAllowed;
    private Long zoneId;

    public SlotUpdateRequest() {
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public VehicleType getVehicleTypeAllowed() {
        return vehicleTypeAllowed;
    }

    public void setVehicleTypeAllowed(VehicleType vehicleTypeAllowed) {
        this.vehicleTypeAllowed = vehicleTypeAllowed;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }
}
