package com.biometric.capture.dto;

public record DashboardEventDto(String type, Long sessionId, int score, String result) {
}
