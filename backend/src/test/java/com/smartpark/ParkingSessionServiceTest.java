package com.smartpark;

import com.smartpark.dto.request.SlotEntryRequest;
import com.smartpark.dto.response.ParkingSessionResponse;
import com.smartpark.entity.*;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ConflictException;
import com.smartpark.exception.ForbiddenException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.UserRepository;
import com.smartpark.repository.VehicleRepository;
import com.smartpark.service.ParkingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSessionServiceTest {

    @Mock
    private ParkingSessionRepository sessionRepository;

    @Mock
    private ParkingSlotRepository slotRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    private ParkingSessionService sessionService;

    private User testUser;
    private Vehicle testVehicle;
    private ParkingZone testZone;
    private ParkingSlot testSlot;

    @BeforeEach
    void setUp() {
        sessionService = new ParkingSessionService(sessionRepository, slotRepository, vehicleRepository, userRepository);

        testUser = new User("alice@campus.edu", "STU-5555", "Alice Smith", "123456", "hash", Role.STUDENT);
        testUser.setId(1L);

        testVehicle = new Vehicle(testUser, "KA-01-MJ-9999", VehicleType.CAR, "Honda City");
        testVehicle.setId(10L);

        testZone = new ParkingZone("ZONE-A", "Main Ground", "Ground", "Standard");
        testZone.setId(100L);

        testSlot = new ParkingSlot("A-001", testZone, SlotStatus.AVAILABLE, VehicleType.CAR);
        testSlot.setId(200L);
    }

    @Test
    @DisplayName("Should process entry successfully and transition slot to OCCUPIED")
    void testProcessEntrySuccess() {
        SlotEntryRequest req = new SlotEntryRequest(10L, 200L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionRepository.existsByUserIdAndStatus(1L, SessionStatus.ACTIVE)).thenReturn(false);
        when(vehicleRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testVehicle));
        when(sessionRepository.existsByVehicleIdAndStatus(10L, SessionStatus.ACTIVE)).thenReturn(false);
        when(slotRepository.findByIdWithLock(200L)).thenReturn(Optional.of(testSlot));

        ParkingSession savedSession = new ParkingSession("SES-1", testUser, testVehicle, testSlot, LocalDateTime.now());
        savedSession.setId(500L);
        when(sessionRepository.save(any(ParkingSession.class))).thenReturn(savedSession);

        ParkingSessionResponse res = sessionService.processEntry(1L, req);

        assertNotNull(res);
        assertEquals(SlotStatus.OCCUPIED, testSlot.getStatus());
        verify(slotRepository).save(testSlot);
        verify(sessionRepository).save(any(ParkingSession.class));
    }

    @Test
    @DisplayName("Should prevent entry if student already has an active session")
    void testProcessEntryStudentActiveSessionConflict() {
        SlotEntryRequest req = new SlotEntryRequest(10L, 200L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionRepository.existsByUserIdAndStatus(1L, SessionStatus.ACTIVE)).thenReturn(true);

        assertThrows(ConflictException.class, () -> sessionService.processEntry(1L, req));
        verify(slotRepository, never()).findByIdWithLock(any());
    }

    @Test
    @DisplayName("Should prevent entry if slot is already occupied (concurrency collision protection)")
    void testProcessEntrySlotAlreadyOccupied() {
        SlotEntryRequest req = new SlotEntryRequest(10L, 200L);
        testSlot.setStatus(SlotStatus.OCCUPIED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sessionRepository.existsByUserIdAndStatus(1L, SessionStatus.ACTIVE)).thenReturn(false);
        when(vehicleRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testVehicle));
        when(sessionRepository.existsByVehicleIdAndStatus(10L, SessionStatus.ACTIVE)).thenReturn(false);
        when(slotRepository.findByIdWithLock(200L)).thenReturn(Optional.of(testSlot));

        assertThrows(ConflictException.class, () -> sessionService.processEntry(1L, req));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should process exit successfully, release slot to AVAILABLE, and calculate duration")
    void testProcessExitSuccess() {
        testSlot.setStatus(SlotStatus.OCCUPIED);
        ParkingSession activeSession = new ParkingSession("SES-1", testUser, testVehicle, testSlot, LocalDateTime.now().minusHours(2));
        activeSession.setId(500L);

        when(sessionRepository.findById(500L)).thenReturn(Optional.of(activeSession));
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSessionResponse res = sessionService.processExit(1L, 500L, "Leaving campus", false);

        assertNotNull(res);
        assertEquals(SessionStatus.COMPLETED, res.getStatus());
        assertEquals(SlotStatus.AVAILABLE, testSlot.getStatus());
        assertTrue(res.getDurationMinutes() >= 120);
        verify(slotRepository).save(testSlot);
    }

    @Test
    @DisplayName("Should forbid exit if student does not own the session")
    void testProcessExitForbiddenForNonOwner() {
        ParkingSession otherSession = new ParkingSession("SES-1", testUser, testVehicle, testSlot, LocalDateTime.now().minusHours(1));
        otherSession.setId(500L);

        when(sessionRepository.findById(500L)).thenReturn(Optional.of(otherSession));

        assertThrows(ForbiddenException.class, () -> sessionService.processExit(999L, 500L, null, false));
    }

    @Test
    @DisplayName("Should prevent duplicate exit on already completed session")
    void testProcessDuplicateExit() {
        ParkingSession completedSession = new ParkingSession("SES-1", testUser, testVehicle, testSlot, LocalDateTime.now().minusHours(3));
        completedSession.setId(500L);
        completedSession.setStatus(SessionStatus.COMPLETED);

        when(sessionRepository.findById(500L)).thenReturn(Optional.of(completedSession));

        assertThrows(ConflictException.class, () -> sessionService.processExit(1L, 500L, null, false));
    }
}
