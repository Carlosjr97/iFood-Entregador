package com.biometric.capture.dto;

public record RankingEntryDto(Long userId, String userName, double averageScore, long sessionsCount) {
}
