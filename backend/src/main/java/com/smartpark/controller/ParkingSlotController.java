package com.smartpark.controller;

import com.smartpark.dto.response.ApiResponse;
import com.smartpark.dto.response.ParkingSlotResponse;
import com.smartpark.dto.response.ParkingZoneResponse;
import com.smartpark.entity.SlotStatus;
import com.smartpark.service.ParkingSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/slots")
public class ParkingSlotController {

    private final ParkingSlotService slotService;

    public ParkingSlotController(ParkingSlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingSlotResponse>>> getSlots(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) SlotStatus status,
            @RequestParam(required = false) String search) {
        List<ParkingSlotResponse> slots = slotService.getAllSlots(zoneId, status, search);
        return ResponseEntity.ok(ApiResponse.ok(slots));
    }

    @GetMapping("/zones")
    public ResponseEntity<ApiResponse<List<ParkingZoneResponse>>> getZones() {
        List<ParkingZoneResponse> zones = slotService.getAllZones();
        return ResponseEntity.ok(ApiResponse.ok(zones));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> getSlotById(@PathVariable Long id) {
        ParkingSlotResponse slot = slotService.getSlotById(id);
        return ResponseEntity.ok(ApiResponse.ok(slot));
    }
}
