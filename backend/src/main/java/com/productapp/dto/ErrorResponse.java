package com.productapp.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> validationErrors;

    public ErrorResponse() {}

    public ErrorResponse(String message, int status, LocalDateTime timestamp, String path, Map<String, String> validationErrors) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}