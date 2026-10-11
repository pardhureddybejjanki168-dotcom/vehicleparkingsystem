package com.smartpark.dto.response;

import com.smartpark.entity.ParkingSession;
import com.smartpark.entity.SessionStatus;
import com.smartpark.entity.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;

public class ParkingSessionResponse {

    private Long id;
    private String sessionCode;
    private Long userId;
    private String userFullName;
    private String studentId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private VehicleType vehicleType;
    private Long slotId;
    private String slotNumber;
    private String zoneName;
    private String zoneFloor;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long durationMinutes;
    private String formattedDuration;
    private SessionStatus status;
    private String notes;

    public ParkingSessionResponse() {
    }

    public static ParkingSessionResponse fromEntity(ParkingSession session) {
        ParkingSessionResponse response = new ParkingSessionResponse();
        response.setId(session.getId());
        response.setSessionCode(session.getSessionCode());

        if (session.getUser() != null) {
            response.setUserId(session.getUser().getId());
            response.setUserFullName(session.getUser().getFullName());
            response.setStudentId(session.getUser().getStudentId());
        }

        if (session.getVehicle() != null) {
            response.setVehicleId(session.getVehicle().getId());
            response.setVehicleRegistrationNumber(session.getVehicle().getRegistrationNumber());
            response.setVehicleType(session.getVehicle().getVehicleType());
        }

        if (session.getSlot() != null) {
            response.setSlotId(session.getSlot().getId());
            response.setSlotNumber(session.getSlot().getSlotNumber());
            if (session.getSlot().getZone() != null) {
                response.setZoneName(session.getSlot().getZone().getName());
                response.setZoneFloor(session.getSlot().getZone().getFloor());
            }
        }

        response.setEntryTime(session.getEntryTime());
        response.setExitTime(session.getExitTime());
        response.setStatus(session.getStatus());
        response.setNotes(session.getNotes());

        // Calculate duration
        long minutes;
        if (session.getDurationMinutes() != null) {
            minutes = session.getDurationMinutes();
        } else if (session.getEntryTime() != null) {
            LocalDateTime end = session.getExitTime() != null ? session.getExitTime() : LocalDateTime.now();
            minutes = Math.max(0, Duration.between(session.getEntryTime(), end).toMinutes());
        } else {
            minutes = 0;
        }

        response.setDurationMinutes(minutes);
        long hours = minutes / 60;
        long remMinutes = minutes % 60;
        if (hours > 0) {
            response.setFormattedDuration(String.format("%dh %02dm", hours, remMinutes));
        } else {
            response.setFormattedDuration(String.format("%d min", remMinutes));
        }

        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
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

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getFormattedDuration() {
        return formattedDuration;
    }

    public void setFormattedDuration(String formattedDuration) {
        this.formattedDuration = formattedDuration;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
