package com.biometric.capture.dto;

import jakarta.validation.constraints.NotBlank;

public record FrameAnalysisRequest(@NotBlank String image) {
}
