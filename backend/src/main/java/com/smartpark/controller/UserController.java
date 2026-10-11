package com.smartpark.controller;

import com.smartpark.dto.response.ApiResponse;
import com.smartpark.dto.response.StudentDashboardStatsResponse;
import com.smartpark.dto.response.UserResponse;
import com.smartpark.security.UserPrincipal;
import com.smartpark.service.AuthService;
import com.smartpark.service.StudentDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final StudentDashboardService dashboardService;
    private final AuthService authService;

    public UserController(StudentDashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<StudentDashboardStatsResponse>> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        StudentDashboardStatsResponse stats = dashboardService.getStudentDashboard(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = authService.getCurrentUserProfile(principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
