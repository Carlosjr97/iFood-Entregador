package com.biometric.capture.service;

import com.biometric.capture.dto.FrameAnalysisResult;
import com.biometric.capture.dto.LivenessResult;
import com.biometric.capture.exception.VisionServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class VisionClientService {

    private static final Logger log = LoggerFactory.getLogger(VisionClientService.class);

    private final RestClient visionRestClient;

    public VisionClientService(RestClient visionRestClient) {
        this.visionRestClient = visionRestClient;
    }

    public FrameAnalysisResult analyzeFrame(String base64Image) {
        try {
            return visionRestClient.post()
                    .uri("/analyze-frame")
                    .body(Map.of("image", base64Image))
                    .retrieve()
                    .body(FrameAnalysisResult.class);
        } catch (RestClientException ex) {
            log.error("event=vision_analyze_failed message=\"{}\"", ex.getMessage());
            throw new VisionServiceException("Falha ao consultar o serviço de visão computacional.", ex);
        }
    }

    public LivenessResult verifyLiveness(List<String> frames, String sessionId) {
        try {
            return visionRestClient.post()
                    .uri("/liveness")
                    .body(Map.of("frames", frames, "sessionId", sessionId))
                    .retrieve()
                    .body(LivenessResult.class);
        } catch (RestClientException ex) {
            log.error("event=vision_liveness_failed message=\"{}\"", ex.getMessage());
            throw new VisionServiceException("Falha ao consultar o serviço de prova de vida.", ex);
        }
    }
}
