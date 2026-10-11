package com.smartpark.dto.response;

import com.smartpark.entity.ParkingZone;

public class ParkingZoneResponse {

    private Long id;
    private String code;
    private String name;
    private String floor;
    private String description;
    private boolean active;
    private int totalSlots;
    private int availableSlots;

    public ParkingZoneResponse() {
    }

    public static ParkingZoneResponse fromEntity(ParkingZone zone) {
        ParkingZoneResponse response = new ParkingZoneResponse();
        response.setId(zone.getId());
        response.setCode(zone.getCode());
        response.setName(zone.getName());
        response.setFloor(zone.getFloor());
        response.setDescription(zone.getDescription());
        response.setActive(zone.isActive());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }
}
