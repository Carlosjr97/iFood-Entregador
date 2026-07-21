package com.biometric.capture.dto;

import java.util.List;

public record FrameAnalysisResult(
        boolean faceDetected,
        double brightness,
        double blur,
        double contrast,
        boolean centered,
        String distance,
        double faceSize,
        BoundingBoxDto boundingBox,
        int score,
        List<String> warnings
) {
}
