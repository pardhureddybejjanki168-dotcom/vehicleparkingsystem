package com.smartpark.controller;

import com.smartpark.dto.request.VehicleRequest;
import com.smartpark.dto.response.ApiResponse;
import com.smartpark.dto.response.VehicleResponse;
import com.smartpark.security.UserPrincipal;
import com.smartpark.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getMyVehicles(@AuthenticationPrincipal UserPrincipal principal) {
        List<VehicleResponse> vehicles = vehicleService.getVehiclesForUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(vehicles));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.registerVehicle(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Vehicle registered successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        vehicleService.deleteVehicle(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle removed successfully", null));
    }
}
