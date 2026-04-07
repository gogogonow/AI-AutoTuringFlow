package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 统一错误响应格式
 */
public class ErrorResponse {

    private String error;
    private String code;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error, String code) {
        this.error = error;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error, String code, Map<String, String> details) {
        this.error = error;
        this.code = code;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
