package com.smartpark.dto.response;

import com.smartpark.entity.ParkingSlot;
import com.smartpark.entity.SlotStatus;
import com.smartpark.entity.VehicleType;

public class ParkingSlotResponse {

    private Long id;
    private String slotNumber;
    private Long zoneId;
    private String zoneCode;
    private String zoneName;
    private String zoneFloor;
    private SlotStatus status;
    private VehicleType vehicleTypeAllowed;
    private VehicleType occupiedVehicleType;
    private String occupiedVehiclePlate;

    public ParkingSlotResponse() {
    }

    public static ParkingSlotResponse fromEntity(ParkingSlot slot) {
        ParkingSlotResponse response = new ParkingSlotResponse();
        response.setId(slot.getId());
        response.setSlotNumber(slot.getSlotNumber());
        if (slot.getZone() != null) {
            response.setZoneId(slot.getZone().getId());
            response.setZoneCode(slot.getZone().getCode());
            response.setZoneName(slot.getZone().getName());
            response.setZoneFloor(slot.getZone().getFloor());
        }
        response.setStatus(slot.getStatus());
        response.setVehicleTypeAllowed(slot.getVehicleTypeAllowed());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getZoneFloor() {
        return zoneFloor;
    }

    public void setZoneFloor(String zoneFloor) {
        this.zoneFloor = zoneFloor;
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

    public VehicleType getOccupiedVehicleType() {
        return occupiedVehicleType;
    }

    public void setOccupiedVehicleType(VehicleType occupiedVehicleType) {
        this.occupiedVehicleType = occupiedVehicleType;
    }

    public String getOccupiedVehiclePlate() {
        return occupiedVehiclePlate;
    }

    public void setOccupiedVehiclePlate(String occupiedVehiclePlate) {
        this.occupiedVehiclePlate = occupiedVehiclePlate;
    }
}
