package com.smartpark.service;

import com.smartpark.dto.response.ParkingSessionResponse;
import com.smartpark.dto.response.StudentDashboardStatsResponse;
import com.smartpark.entity.SessionStatus;
import com.smartpark.entity.SlotStatus;
import com.smartpark.entity.User;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.UserRepository;
import com.smartpark.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentDashboardService {

    private final UserRepository userRepository;
    private final ParkingSessionRepository sessionRepository;
    private final ParkingSlotRepository slotRepository;
    private final VehicleRepository vehicleRepository;

    public StudentDashboardService(UserRepository userRepository,
                                   ParkingSessionRepository sessionRepository,
                                   ParkingSlotRepository slotRepository,
                                   VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.slotRepository = slotRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardStatsResponse getStudentDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        StudentDashboardStatsResponse response = new StudentDashboardStatsResponse();
        response.setStudentName(user.getFullName());

        // Current active session
        sessionRepository.findFirstByUserIdAndStatusOrderByEntryTimeDesc(userId, SessionStatus.ACTIVE)
                .ifPresent(active -> response.setActiveSession(ParkingSessionResponse.fromEntity(active)));

        response.setTotalRegisteredVehicles(vehicleRepository.countByUserId(userId));
        response.setAvailableSlotsCount(slotRepository.countByStatus(SlotStatus.AVAILABLE));
        response.setTotalSlotsCount(slotRepository.count());

        List<ParkingSessionResponse> recent = sessionRepository.findTop5ByUserIdOrderByEntryTimeDesc(userId)
                .stream()
                .map(ParkingSessionResponse::fromEntity)
                .collect(Collectors.toList());
        response.setRecentSessions(recent);

        return response;
    }
}
