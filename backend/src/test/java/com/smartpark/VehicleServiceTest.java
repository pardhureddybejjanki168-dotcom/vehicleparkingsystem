package com.smartpark;

import com.smartpark.dto.request.VehicleRequest;
import com.smartpark.dto.response.VehicleResponse;
import com.smartpark.entity.Role;
import com.smartpark.entity.SessionStatus;
import com.smartpark.entity.User;
import com.smartpark.entity.Vehicle;
import com.smartpark.entity.VehicleType;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ConflictException;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.UserRepository;
import com.smartpark.repository.VehicleRepository;
import com.smartpark.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParkingSessionRepository sessionRepository;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, userRepository, sessionRepository);
    }

    @Test
    @DisplayName("Should normalize registration number and register vehicle successfully")
    void testRegisterVehicleSuccess() {
        User user = new User("car@owner.com", "STU-88", "Bob", null, "hash", Role.STUDENT);
        user.setId(5L);

        VehicleRequest req = new VehicleRequest("mh 12 ab 1234", VehicleType.CAR, "Honda City");

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(vehicleRepository.existsByRegistrationNumber("MH-12-AB-1234")).thenReturn(false);

        Vehicle saved = new Vehicle(user, "MH-12-AB-1234", VehicleType.CAR, "Honda City");
        saved.setId(15L);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(saved);

        VehicleResponse res = vehicleService.registerVehicle(5L, req);

        assertNotNull(res);
        assertEquals("MH-12-AB-1234", res.getRegistrationNumber());
        assertEquals(VehicleType.CAR, res.getVehicleType());
    }

    @Test
    @DisplayName("Should reject duplicate vehicle registration")
    void testRegisterDuplicateVehicle() {
        User user = new User("car@owner.com", "STU-88", "Bob", null, "hash", Role.STUDENT);
        user.setId(5L);

        VehicleRequest req = new VehicleRequest("DL-01-XX-9999", VehicleType.CAR, "Ford");

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(vehicleRepository.existsByRegistrationNumber("DL-01-XX-9999")).thenReturn(true);

        assertThrows(ConflictException.class, () -> vehicleService.registerVehicle(5L, req));
    }

    @Test
    @DisplayName("Should prevent deleting vehicle while in an active session")
    void testDeleteVehicleWithActiveSession() {
        User user = new User("car@owner.com", "STU-88", "Bob", null, "hash", Role.STUDENT);
        user.setId(5L);

        Vehicle v = new Vehicle(user, "DL-01-XX-9999", VehicleType.CAR, "Ford");
        v.setId(22L);

        when(vehicleRepository.findByIdAndUserId(22L, 5L)).thenReturn(Optional.of(v));
        when(sessionRepository.existsByVehicleIdAndStatus(22L, SessionStatus.ACTIVE)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> vehicleService.deleteVehicle(5L, 22L));
        verify(vehicleRepository, never()).delete(any());
    }
}
