package com.biometric.capture.dto;

import java.util.List;
import java.util.Map;

public record DashboardStatsDto(
        long totalSessions,
        double averageScore,
        double averageDurationSeconds,
        Map<String, Long> failuresByCategory,
        List<EvolutionPointDto> evolution
) {
}
