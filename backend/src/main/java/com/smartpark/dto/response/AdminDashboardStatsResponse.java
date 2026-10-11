package com.smartpark.dto.response;

import java.util.List;
import java.util.Map;

public class AdminDashboardStatsResponse {

    private long totalUsers;
    private long totalSlots;
    private long availableSlots;
    private long occupiedSlots;
    private long outOfServiceSlots;
    private long activeSessions;
    private long todayEntries;
    private long todayExits;
    private double occupancyRatePercent;
    private Map<String, Long> zoneOccupancy;
    private List<ParkingSessionResponse> recentSessions;
    private List<UserResponse> recentRegistrations;

    public AdminDashboardStatsResponse() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(long totalSlots) {
        this.totalSlots = totalSlots;
    }

    public long getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(long availableSlots) {
        this.availableSlots = availableSlots;
    }

    public long getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(long occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public long getOutOfServiceSlots() {
        return outOfServiceSlots;
    }

    public void setOutOfServiceSlots(long outOfServiceSlots) {
        this.outOfServiceSlots = outOfServiceSlots;
    }

    public long getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(long activeSessions) {
        this.activeSessions = activeSessions;
    }

    public long getTodayEntries() {
        return todayEntries;
    }

    public void setTodayEntries(long todayEntries) {
        this.todayEntries = todayEntries;
    }

    public long getTodayExits() {
        return todayExits;
    }

    public void setTodayExits(long todayExits) {
        this.todayExits = todayExits;
    }

    public double getOccupancyRatePercent() {
        return occupancyRatePercent;
    }

    public void setOccupancyRatePercent(double occupancyRatePercent) {
        this.occupancyRatePercent = occupancyRatePercent;
    }

    public Map<String, Long> getZoneOccupancy() {
        return zoneOccupancy;
    }

    public void setZoneOccupancy(Map<String, Long> zoneOccupancy) {
        this.zoneOccupancy = zoneOccupancy;
    }

    public List<ParkingSessionResponse> getRecentSessions() {
        return recentSessions;
    }

    public void setRecentSessions(List<ParkingSessionResponse> recentSessions) {
        this.recentSessions = recentSessions;
    }

    public List<UserResponse> getRecentRegistrations() {
        return recentRegistrations;
    }

    public void setRecentRegistrations(List<UserResponse> recentRegistrations) {
        this.recentRegistrations = recentRegistrations;
    }
}
