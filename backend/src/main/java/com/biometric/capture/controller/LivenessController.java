package com.biometric.capture.controller;

import com.biometric.capture.dto.LivenessResult;
import com.biometric.capture.dto.LivenessVerifyRequest;
import com.biometric.capture.service.VisionClientService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/liveness")
public class LivenessController {

    private final VisionClientService visionClientService;

    public LivenessController(VisionClientService visionClientService) {
        this.visionClientService = visionClientService;
    }

    @PostMapping("/verify")
    public LivenessResult verify(@Valid @RequestBody LivenessVerifyRequest request) {
        return visionClientService.verifyLiveness(request.frames(), String.valueOf(request.sessionId()));
    }
}
