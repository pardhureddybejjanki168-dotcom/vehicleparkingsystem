package com.smartpark.controller;

import com.smartpark.dto.request.SlotEntryRequest;
import com.smartpark.dto.request.SlotExitRequest;
import com.smartpark.dto.response.ApiResponse;
import com.smartpark.dto.response.ParkingSessionResponse;
import com.smartpark.entity.SessionStatus;
import com.smartpark.security.UserPrincipal;
import com.smartpark.service.ParkingSessionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sessions")
public class ParkingSessionController {

    private final ParkingSessionService sessionService;

    public ParkingSessionController(ParkingSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/entry")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> processEntry(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SlotEntryRequest request) {
        ParkingSessionResponse response = sessionService.processEntry(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Parking entry confirmed. Slot assigned successfully.", response));
    }

    @PostMapping("/exit")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> processExit(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SlotExitRequest request) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ParkingSessionResponse response = sessionService.processExit(principal.getId(), request.getSessionId(), request.getNotes(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Parking exit confirmed. Slot has been released.", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> getActiveSession(@AuthenticationPrincipal UserPrincipal principal) {
        return sessionService.getActiveSessionForUser(principal.getId())
                .map(s -> ResponseEntity.ok(ApiResponse.ok(s)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("No active parking session", null)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<ParkingSessionResponse>>> getParkingHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("entryTime").descending());
        Page<ParkingSessionResponse> history = sessionService.getStudentHistory(principal.getId(), status, search, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
