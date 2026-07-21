package com.biometric.capture.dto;

import com.biometric.capture.domain.SessionResult;

import java.time.Instant;

public record SessionDto(Long id, Long userId, String userName, int score, SessionResult result, Instant createdAt) {
}
