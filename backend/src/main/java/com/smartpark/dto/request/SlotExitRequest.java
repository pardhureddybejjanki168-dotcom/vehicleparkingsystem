package com.smartpark.dto.request;

import jakarta.validation.constraints.NotNull;

public class SlotExitRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    private String notes;

    public SlotExitRequest() {
    }

    public SlotExitRequest(Long sessionId, String notes) {
        this.sessionId = sessionId;
        this.notes = notes;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
