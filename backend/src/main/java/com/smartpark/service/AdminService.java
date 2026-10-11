package com.smartpark.service;

import com.smartpark.dto.response.*;
import com.smartpark.entity.ParkingSlot;
import com.smartpark.entity.Role;
import com.smartpark.entity.SessionStatus;
import com.smartpark.entity.SlotStatus;
import com.smartpark.entity.User;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.ParkingZoneRepository;
import com.smartpark.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ParkingSlotRepository slotRepository;
    private final ParkingZoneRepository zoneRepository;
    private final ParkingSessionRepository sessionRepository;

    public AdminService(UserRepository userRepository,
                        ParkingSlotRepository slotRepository,
                        ParkingZoneRepository zoneRepository,
                        ParkingSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.zoneRepository = zoneRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getAdminDashboardStats() {
        AdminDashboardStatsResponse stats = new AdminDashboardStatsResponse();

        long totalUsers = userRepository.countByRole(Role.STUDENT);
        long totalSlots = slotRepository.count();
        long availableSlots = slotRepository.countByStatus(SlotStatus.AVAILABLE);
        long occupiedSlots = slotRepository.countByStatus(SlotStatus.OCCUPIED);
        long outOfServiceSlots = slotRepository.countByStatus(SlotStatus.OUT_OF_SERVICE);
        long activeSessions = sessionRepository.countByStatus(SessionStatus.ACTIVE);

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        long todayEntries = sessionRepository.countByEntryTimeBetween(startOfToday, endOfToday);
        long todayExits = sessionRepository.countByExitTimeBetween(startOfToday, endOfToday);

        double occupancyRate = totalSlots > 0 ? ((double) occupiedSlots / totalSlots) * 100.0 : 0.0;

        stats.setTotalUsers(totalUsers);
        stats.setTotalSlots(totalSlots);
        stats.setAvailableSlots(availableSlots);
        stats.setOccupiedSlots(occupiedSlots);
        stats.setOutOfServiceSlots(outOfServiceSlots);
        stats.setActiveSessions(activeSessions);
        stats.setTodayEntries(todayEntries);
        stats.setTodayExits(todayExits);
        stats.setOccupancyRatePercent(Math.round(occupancyRate * 10.0) / 10.0);

        // Zone Occupancy Breakdown
        Map<String, Long> zoneOccupancy = new HashMap<>();
        zoneRepository.findAll().forEach(zone -> {
            List<ParkingSlot> slots = slotRepository.findByZoneId(zone.getId());
            long occupied = slots.stream().filter(s -> s.getStatus() == SlotStatus.OCCUPIED).count();
            zoneOccupancy.put(zone.getName(), occupied);
        });
        stats.setZoneOccupancy(zoneOccupancy);

        // Recent Sessions (latest 5)
        Page<ParkingSessionResponse> recentSessions = sessionRepository.filterAdminSessions(
                null, null, null, null, PageRequest.of(0, 5, Sort.by("entryTime").descending())
        ).map(ParkingSessionResponse::fromEntity);
        stats.setRecentSessions(recentSessions.getContent());

        // Recent Registrations (latest 5)
        Page<User> recentUsers = userRepository.findAllByRole(
                Role.STUDENT, PageRequest.of(0, 5, Sort.by("createdAt").descending())
        );
        stats.setRecentRegistrations(recentUsers.getContent().stream().map(UserResponse::fromEntity).collect(Collectors.toList()));

        return stats;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getStudents(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.searchUsersByRole(Role.STUDENT, search.trim(), pageable)
                    .map(UserResponse::fromEntity);
        }
        return userRepository.findAllByRole(Role.STUDENT, pageable)
                .map(UserResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot alter administrator activation status");
        }

        user.setEnabled(enabled);
        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }
}
