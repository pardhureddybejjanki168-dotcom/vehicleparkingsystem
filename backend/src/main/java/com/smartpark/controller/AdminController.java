package com.smartpark.controller;

import com.smartpark.dto.request.SlotCreateRequest;
import com.smartpark.dto.request.SlotUpdateRequest;
import com.smartpark.dto.response.*;
import com.smartpark.entity.SessionStatus;
import com.smartpark.security.UserPrincipal;
import com.smartpark.service.AdminService;
import com.smartpark.service.ParkingSessionService;
import com.smartpark.service.ParkingSlotService;
import com.smartpark.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ParkingSlotService slotService;
    private final ParkingSessionService sessionService;
    private final ReportService reportService;

    public AdminController(AdminService adminService,
                           ParkingSlotService slotService,
                           ParkingSessionService sessionService,
                           ReportService reportService) {
        this.adminService = adminService;
        this.slotService = slotService;
        this.sessionService = sessionService;
        this.reportService = reportService;
    }

    // 1. Dashboard Analytics
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getStats() {
        AdminDashboardStatsResponse stats = adminService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    // 2. User Management
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserResponse> students = adminService.getStudents(search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(students));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserDetails(@PathVariable Long id) {
        UserResponse user = adminService.getUserDetails(id);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        UserResponse updated = adminService.toggleUserStatus(id, enabled);
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully", updated));
    }

    // 3. Slot Management
    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> createSlot(@Valid @RequestBody SlotCreateRequest request) {
        ParkingSlotResponse slot = slotService.createSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Parking slot created successfully", slot));
    }

    @PutMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> updateSlot(
            @PathVariable Long id,
            @RequestBody SlotUpdateRequest request) {
        ParkingSlotResponse updated = slotService.updateSlot(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Parking slot updated successfully", updated));
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.ok("Parking slot deleted successfully", null));
    }

    // 4. Session & Parking Operations Management
    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<List<ParkingSessionResponse>>> getActiveSessions() {
        List<ParkingSessionResponse> sessions = sessionService.getAllActiveSessions();
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @GetMapping("/sessions/history")
    public ResponseEntity<ApiResponse<Page<ParkingSessionResponse>>> getCampusHistory(
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("entryTime").descending());
        Page<ParkingSessionResponse> history = sessionService.getAdminHistory(status, search, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @PostMapping("/sessions/{id}/force-exit")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> forceExit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Recorded by Administrator") String notes) {
        ParkingSessionResponse response = sessionService.processExit(principal.getId(), id, notes, true);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle exit successfully recorded by admin", response));
    }

    // 5. Reports & CSV Export
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportSessionsCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        byte[] csvBytes = reportService.exportParkingSessionsToCsv(fromDate, toDate);
        String filename = "smartpark-report-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
