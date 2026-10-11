package com.smartpark.service;

import com.smartpark.dto.request.SlotCreateRequest;
import com.smartpark.dto.request.SlotUpdateRequest;
import com.smartpark.dto.response.ParkingSlotResponse;
import com.smartpark.dto.response.ParkingZoneResponse;
import com.smartpark.entity.ParkingSlot;
import com.smartpark.entity.ParkingZone;
import com.smartpark.entity.SessionStatus;
import com.smartpark.entity.SlotStatus;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ConflictException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.ParkingZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingSlotService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingZoneRepository zoneRepository;
    private final ParkingSessionRepository sessionRepository;

    public ParkingSlotService(ParkingSlotRepository slotRepository,
                              ParkingZoneRepository zoneRepository,
                              ParkingSessionRepository sessionRepository) {
        this.slotRepository = slotRepository;
        this.zoneRepository = zoneRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<ParkingSlotResponse> getAllSlots(Long zoneId, SlotStatus status, String search) {
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<ParkingSlot> slots = slotRepository.filterSlots(zoneId, status, trimmedSearch);
        
        // Load active sessions map to populate occupied vehicle details
        java.util.Map<Long, com.smartpark.entity.ParkingSession> activeSessionsBySlot = sessionRepository
                .findByStatusOrderByEntryTimeDesc(SessionStatus.ACTIVE)
                .stream()
                .filter(s -> s.getSlot() != null)
                .collect(Collectors.toMap(s -> s.getSlot().getId(), s -> s, (s1, s2) -> s1));

        return slots.stream().map(slot -> {
            ParkingSlotResponse dto = ParkingSlotResponse.fromEntity(slot);
            if (slot.getStatus() == SlotStatus.OCCUPIED) {
                com.smartpark.entity.ParkingSession activeSession = activeSessionsBySlot.get(slot.getId());
                if (activeSession != null && activeSession.getVehicle() != null) {
                    dto.setOccupiedVehicleType(activeSession.getVehicle().getVehicleType());
                    dto.setOccupiedVehiclePlate(activeSession.getVehicle().getRegistrationNumber());
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParkingSlotResponse getSlotById(Long id) {
        ParkingSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + id));
        ParkingSlotResponse dto = ParkingSlotResponse.fromEntity(slot);
        if (slot.getStatus() == SlotStatus.OCCUPIED) {
            sessionRepository.findFirstBySlotIdAndStatus(slot.getId(), SessionStatus.ACTIVE)
                    .ifPresent(s -> {
                        if (s.getVehicle() != null) {
                            dto.setOccupiedVehicleType(s.getVehicle().getVehicleType());
                            dto.setOccupiedVehiclePlate(s.getVehicle().getRegistrationNumber());
                        }
                    });
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ParkingZoneResponse> getAllZones() {
        return zoneRepository.findAll().stream().map(zone -> {
            ParkingZoneResponse dto = ParkingZoneResponse.fromEntity(zone);
            List<ParkingSlot> slots = slotRepository.findByZoneId(zone.getId());
            dto.setTotalSlots(slots.size());
            dto.setAvailableSlots((int) slots.stream().filter(s -> s.getStatus() == SlotStatus.AVAILABLE).count());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ParkingSlotResponse createSlot(SlotCreateRequest request) {
        String normalizedSlotNumber = request.getSlotNumber().trim().toUpperCase();

        if (slotRepository.existsBySlotNumber(normalizedSlotNumber)) {
            throw new ConflictException("Slot number " + normalizedSlotNumber + " already exists");
        }

        ParkingZone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking zone not found with id: " + request.getZoneId()));

        ParkingSlot slot = new ParkingSlot(
                normalizedSlotNumber,
                zone,
                SlotStatus.AVAILABLE,
                request.getVehicleTypeAllowed()
        );

        ParkingSlot saved = slotRepository.save(slot);
        return ParkingSlotResponse.fromEntity(saved);
    }

    @Transactional
    public ParkingSlotResponse updateSlot(Long id, SlotUpdateRequest request) {
        ParkingSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + id));

        // Requirement: Prevent conflicting changes to slots with active sessions
        boolean hasActiveSession = sessionRepository.existsBySlotIdAndStatus(slot.getId(), SessionStatus.ACTIVE);

        if (request.getStatus() != null) {
            if (hasActiveSession && request.getStatus() != SlotStatus.OCCUPIED) {
                throw new BadRequestException("Cannot alter slot status while an active vehicle session occupies it");
            }
            slot.setStatus(request.getStatus());
        }

        if (request.getZoneId() != null) {
            if (hasActiveSession) {
                throw new BadRequestException("Cannot reassign zone for a slot with an active parking session");
            }
            ParkingZone zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));
            slot.setZone(zone);
        }

        if (request.getVehicleTypeAllowed() != null) {
            slot.setVehicleTypeAllowed(request.getVehicleTypeAllowed());
        }

        ParkingSlot saved = slotRepository.save(slot);
        return ParkingSlotResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteSlot(Long id) {
        ParkingSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found"));

        if (sessionRepository.existsBySlotIdAndStatus(slot.getId(), SessionStatus.ACTIVE)) {
            throw new BadRequestException("Cannot delete slot with an active parking session");
        }

        slotRepository.delete(slot);
    }
}
