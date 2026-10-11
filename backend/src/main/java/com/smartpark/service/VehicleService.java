package com.smartpark.service;

import com.smartpark.dto.request.VehicleRequest;
import com.smartpark.dto.response.VehicleResponse;
import com.smartpark.entity.SessionStatus;
import com.smartpark.entity.User;
import com.smartpark.entity.Vehicle;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ConflictException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.UserRepository;
import com.smartpark.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ParkingSessionRepository sessionRepository;

    public VehicleService(VehicleRepository vehicleRepository,
                          UserRepository userRepository,
                          ParkingSessionRepository sessionRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public static String normalizeRegistrationNumber(String regNo) {
        if (regNo == null) return null;
        return regNo.trim().toUpperCase().replaceAll("\\s+", "-");
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesForUser(Long userId) {
        return vehicleRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse registerVehicle(Long userId, VehicleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String normalizedReg = normalizeRegistrationNumber(request.getRegistrationNumber());

        if (vehicleRepository.existsByRegistrationNumber(normalizedReg)) {
            throw new ConflictException("A vehicle with registration number " + normalizedReg + " is already registered in the system.");
        }

        Vehicle vehicle = new Vehicle(
                user,
                normalizedReg,
                request.getVehicleType(),
                request.getMakeModel() != null ? request.getMakeModel().trim() : null
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteVehicle(Long userId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found or does not belong to you"));

        // Check if vehicle is in an active session
        if (sessionRepository.existsByVehicleIdAndStatus(vehicle.getId(), SessionStatus.ACTIVE)) {
            throw new BadRequestException("Cannot delete vehicle while it has an active parking session");
        }

        vehicleRepository.delete(vehicle);
    }
}
