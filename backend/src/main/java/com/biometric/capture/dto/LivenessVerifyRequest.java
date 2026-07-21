package com.biometric.capture.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LivenessVerifyRequest(
        @NotNull Long sessionId,
        @NotEmpty List<String> frames
) {
}
