package com.smartpark.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    private boolean success;
    private String message;
    private List<String> errors;
    private int status;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }

    public ErrorResponse(int status, String message) {
        this.success = false;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }

    public ErrorResponse(int status, String message, List<String> errors) {
        this.success = false;
        this.status = status;
        this.message = message;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
