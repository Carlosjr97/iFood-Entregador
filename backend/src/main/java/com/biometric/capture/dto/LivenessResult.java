package com.biometric.capture.dto;

public record LivenessResult(
        boolean leftTurn,
        boolean rightTurn,
        boolean centerReturn,
        boolean completed,
        int framesAnalyzed,
        double facesDetectedRatio
) {
}
