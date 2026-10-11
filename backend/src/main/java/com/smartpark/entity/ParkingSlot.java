package com.smartpark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_slots", indexes = {
    @Index(name = "idx_slot_number", columnList = "slot_number"),
    @Index(name = "idx_slot_status", columnList = "status")
})
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_number", nullable = false, unique = true, length = 30)
    private String slotNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id", nullable = false)
    private ParkingZone zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type_allowed", length = 30)
    private VehicleType vehicleTypeAllowed = VehicleType.CAR;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ParkingSlot() {
    }

    public ParkingSlot(String slotNumber, ParkingZone zone, SlotStatus status, VehicleType vehicleTypeAllowed) {
        this.slotNumber = slotNumber;
        this.zone = zone;
        this.status = status != null ? status : SlotStatus.AVAILABLE;
        this.vehicleTypeAllowed = vehicleTypeAllowed != null ? vehicleTypeAllowed : VehicleType.CAR;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public ParkingZone getZone() {
        return zone;
    }

    public void setZone(ParkingZone zone) {
        this.zone = zone;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
