package com.biometric.capture.dto;

import java.time.LocalDate;

public record EvolutionPointDto(LocalDate date, double averageScore, long sessionsCount) {
}
