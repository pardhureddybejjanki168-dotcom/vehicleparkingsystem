package com.smartpark.service;

import com.smartpark.dto.request.SlotEntryRequest;
import com.smartpark.dto.response.ParkingSessionResponse;
import com.smartpark.entity.*;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ConflictException;
import com.smartpark.exception.ForbiddenException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.UserRepository;
import com.smartpark.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ParkingSessionService {

    private final ParkingSessionRepository sessionRepository;
    private final ParkingSlotRepository slotRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public ParkingSessionService(ParkingSessionRepository sessionRepository,
                                 ParkingSlotRepository slotRepository,
                                 VehicleRepository vehicleRepository,
                                 UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.slotRepository = slotRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ParkingSessionResponse processEntry(Long userId, SlotEntryRequest request) {
        // 1. Revalidate User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isEnabled()) {
            throw new BadRequestException("User account is deactivated");
        }

        // 2. Revalidate Vehicle ownership

        // 3. Revalidate Vehicle ownership
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(request.getVehicleId(), userId)
                .orElseThrow(() -> new BadRequestException("Vehicle does not exist or does not belong to your account"));

        // 4. Prevent multiple active sessions for the same vehicle
        if (sessionRepository.existsByVehicleIdAndStatus(vehicle.getId(), SessionStatus.ACTIVE)) {
            throw new ConflictException("This vehicle is already in an active parking session.");
        }

        // 5. Revalidate Slot with pessimistic lock for concurrent collision protection
        ParkingSlot slot = slotRepository.findByIdWithLock(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found"));

        if (slot.getStatus() == SlotStatus.OCCUPIED) {
            throw new ConflictException("Parking slot " + slot.getSlotNumber() + " is already occupied. Please select another slot.");
        }

        if (slot.getStatus() == SlotStatus.OUT_OF_SERVICE) {
            throw new BadRequestException("Parking slot " + slot.getSlotNumber() + " is currently out of service.");
        }

        // 6. Transition slot to OCCUPIED transactionally
        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);

        // 7. Generate unique session reference code
        String sessionCode = "PK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // 8. Server-authoritative entry timestamp
        LocalDateTime entryTime = LocalDateTime.now();

        ParkingSession session = new ParkingSession(sessionCode, user, vehicle, slot, entryTime);
        ParkingSession savedSession = sessionRepository.save(session);

        return ParkingSessionResponse.fromEntity(savedSession);
    }

    @Transactional
    public ParkingSessionResponse processExit(Long userId, Long sessionId, String notes, boolean isAdmin) {
        ParkingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking session not found with id: " + sessionId));

        // Ownership enforcement for student role
        if (!isAdmin && !session.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to exit this parking session");
        }

        // Prevent duplicate exit processing
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new ConflictException("This parking session is already marked as " + session.getStatus().name());
        }

        // Server-generated exit timestamp
        LocalDateTime exitTime = LocalDateTime.now();
        session.setExitTime(exitTime);

        // Calculate parking duration in minutes
        long minutes = Math.max(1, Duration.between(session.getEntryTime(), exitTime).toMinutes());
        session.setDurationMinutes(minutes);
        session.setStatus(SessionStatus.COMPLETED);

        if (notes != null && !notes.isBlank()) {
            session.setNotes(notes.trim());
        }

        // Release the assigned slot transactionally
        ParkingSlot slot = session.getSlot();
        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            slotRepository.save(slot);
        }

        ParkingSession completedSession = sessionRepository.save(session);
        return ParkingSessionResponse.fromEntity(completedSession);
    }

    @Transactional(readOnly = true)
    public Optional<ParkingSessionResponse> getActiveSessionForUser(Long userId) {
        return sessionRepository.findFirstByUserIdAndStatusOrderByEntryTimeDesc(userId, SessionStatus.ACTIVE)
                .map(ParkingSessionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ParkingSessionResponse> getAllActiveSessions() {
        return sessionRepository.findByStatusOrderByEntryTimeDesc(SessionStatus.ACTIVE)
                .stream()
                .map(ParkingSessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ParkingSessionResponse> getStudentHistory(Long userId,
                                                          SessionStatus status,
                                                          String search,
                                                          LocalDate fromDate,
                                                          LocalDate toDate,
                                                          Pageable pageable) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        return sessionRepository.filterStudentSessions(userId, status, trimmedSearch, start, end, pageable)
                .map(ParkingSessionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ParkingSessionResponse> getAdminHistory(SessionStatus status,
                                                        String search,
                                                        LocalDate fromDate,
                                                        LocalDate toDate,
                                                        Pageable pageable) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        return sessionRepository.filterAdminSessions(status, trimmedSearch, start, end, pageable)
                .map(ParkingSessionResponse::fromEntity);
    }
}
