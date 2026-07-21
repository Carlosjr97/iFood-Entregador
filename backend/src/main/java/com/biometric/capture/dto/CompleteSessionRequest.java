package com.biometric.capture.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompleteSessionRequest(
        @Min(0) @Max(100) int score,
        double brightness,
        double blur,
        double faceSize,
        boolean centered,
        double stability,
        @NotNull Boolean livenessCompleted
) {
}
