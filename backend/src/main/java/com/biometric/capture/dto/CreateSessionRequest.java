package com.biometric.capture.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSessionRequest(@NotNull Long userId) {
}
