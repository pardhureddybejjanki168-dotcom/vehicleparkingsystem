package com.smartpark;

import com.smartpark.dto.response.AdminDashboardStatsResponse;
import com.smartpark.dto.response.UserResponse;
import com.smartpark.entity.*;
import com.smartpark.exception.BadRequestException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.ParkingZoneRepository;
import com.smartpark.repository.UserRepository;
import com.smartpark.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParkingSlotRepository slotRepository;

    @Mock
    private ParkingZoneRepository zoneRepository;

    @Mock
    private ParkingSessionRepository sessionRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, slotRepository, zoneRepository, sessionRepository);
    }

    @Test
    @DisplayName("Should accurately calculate Admin dashboard stats from database data")
    void testGetAdminDashboardStats() {
        when(userRepository.countByRole(Role.STUDENT)).thenReturn(50L);
        when(slotRepository.count()).thenReturn(20L);
        when(slotRepository.countByStatus(SlotStatus.AVAILABLE)).thenReturn(15L);
        when(slotRepository.countByStatus(SlotStatus.OCCUPIED)).thenReturn(4L);
        when(slotRepository.countByStatus(SlotStatus.OUT_OF_SERVICE)).thenReturn(1L);
        when(sessionRepository.countByStatus(SessionStatus.ACTIVE)).thenReturn(4L);
        when(sessionRepository.countByEntryTimeBetween(any(), any())).thenReturn(10L);
        when(sessionRepository.countByExitTimeBetween(any(), any())).thenReturn(6L);
        when(zoneRepository.findAll()).thenReturn(Collections.emptyList());
        when(sessionRepository.filterAdminSessions(any(), any(), any(), any(), any())).thenReturn(Page.empty());
        when(userRepository.findAllByRole(eq(Role.STUDENT), any(Pageable.class))).thenReturn(Page.empty());

        AdminDashboardStatsResponse stats = adminService.getAdminDashboardStats();

        assertNotNull(stats);
        assertEquals(50L, stats.getTotalUsers());
        assertEquals(20L, stats.getTotalSlots());
        assertEquals(15L, stats.getAvailableSlots());
        assertEquals(4L, stats.getOccupiedSlots());
        assertEquals(1L, stats.getOutOfServiceSlots());
        assertEquals(4L, stats.getActiveSessions());
        assertEquals(10L, stats.getTodayEntries());
        assertEquals(6L, stats.getTodayExits());
        assertEquals(20.0, stats.getOccupancyRatePercent()); // 4 / 20 * 100
    }

    @Test
    @DisplayName("Should activate or deactivate student account")
    void testToggleUserStatus() {
        User student = new User("stu@test.edu", "STU-01", "Student One", null, "hash", Role.STUDENT);
        student.setId(3L);
        student.setEnabled(true);

        when(userRepository.findById(3L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse res = adminService.toggleUserStatus(3L, false);

        assertNotNull(res);
        assertFalse(res.isEnabled());
    }

    @Test
    @DisplayName("Should prevent altering admin account activation status")
    void testToggleAdminStatusForbidden() {
        User admin = new User("admin@test.edu", "ADM-01", "Admin One", null, "hash", Role.ADMIN);
        admin.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(BadRequestException.class, () -> adminService.toggleUserStatus(1L, false));
    }
}
