package com.smartpark.dto.response;

import java.util.List;

public class StudentDashboardStatsResponse {

    private String studentName;
    private ParkingSessionResponse activeSession;
    private long totalRegisteredVehicles;
    private long availableSlotsCount;
    private long totalSlotsCount;
    private List<ParkingSessionResponse> recentSessions;

    public StudentDashboardStatsResponse() {
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public ParkingSessionResponse getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(ParkingSessionResponse activeSession) {
        this.activeSession = activeSession;
    }

    public long getTotalRegisteredVehicles() {
        return totalRegisteredVehicles;
    }

    public void setTotalRegisteredVehicles(long totalRegisteredVehicles) {
        this.totalRegisteredVehicles = totalRegisteredVehicles;
    }

    public long getAvailableSlotsCount() {
        return availableSlotsCount;
    }

    public void setAvailableSlotsCount(long availableSlotsCount) {
        this.availableSlotsCount = availableSlotsCount;
    }

    public long getTotalSlotsCount() {
        return totalSlotsCount;
    }

    public void setTotalSlotsCount(long totalSlotsCount) {
        this.totalSlotsCount = totalSlotsCount;
    }

    public List<ParkingSessionResponse> getRecentSessions() {
        return recentSessions;
    }

    public void setRecentSessions(List<ParkingSessionResponse> recentSessions) {
        this.recentSessions = recentSessions;
    }
}
