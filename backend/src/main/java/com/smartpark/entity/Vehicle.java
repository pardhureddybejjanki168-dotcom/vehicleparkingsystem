package com.smartpark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicle_reg_no", columnList = "registration_number"),
    @Index(name = "idx_vehicle_user", columnList = "user_id")
})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "registration_number", nullable = false, unique = true, length = 30)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType = VehicleType.CAR;

    @Column(name = "make_model", length = 100)
    private String makeModel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Vehicle() {
    }

    public Vehicle(User user, String registrationNumber, VehicleType vehicleType, String makeModel) {
        this.user = user;
        this.registrationNumber = registrationNumber != null ? registrationNumber.toUpperCase().trim() : null;
        this.vehicleType = vehicleType != null ? vehicleType : VehicleType.CAR;
        this.makeModel = makeModel;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.registrationNumber != null) {
            this.registrationNumber = this.registrationNumber.toUpperCase().trim();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber != null ? registrationNumber.toUpperCase().trim() : null;
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
}
