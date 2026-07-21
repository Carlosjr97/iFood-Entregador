package com.biometric.capture.controller;

import com.biometric.capture.dto.FrameAnalysisRequest;
import com.biometric.capture.dto.FrameAnalysisResult;
import com.biometric.capture.service.VisionClientService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST fallback for single-frame analysis (curl/testing/non-WebSocket clients). The Capture
 * page itself uses the /ws/capture WebSocket for the continuous real-time stream.
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final VisionClientService visionClientService;

    public AnalysisController(VisionClientService visionClientService) {
        this.visionClientService = visionClientService;
    }

    @PostMapping("/frame")
    public FrameAnalysisResult analyzeFrame(@Valid @RequestBody FrameAnalysisRequest request) {
        return visionClientService.analyzeFrame(request.image());
    }
}
