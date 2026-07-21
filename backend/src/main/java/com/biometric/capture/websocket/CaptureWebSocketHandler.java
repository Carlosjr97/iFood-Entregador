package com.biometric.capture.websocket;

import com.biometric.capture.dto.FrameAnalysisResult;
import com.biometric.capture.exception.VisionServiceException;
import com.biometric.capture.service.StabilityCalculator;
import com.biometric.capture.service.VisionClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real-time capture channel: the Angular client streams webcam frames as base64 JPEGs over
 * this socket (~every 600ms), each is forwarded synchronously to the vision-service, and the
 * quality result (plus a server-computed "estabilidade" score built from recent face
 * positions) is streamed back on the same connection. Blocking per-message I/O is acceptable
 * here because virtual threads are enabled (spring.threads.virtual.enabled) — this is a demo
 * / low-concurrency assistant, not a high-throughput service.
 */
@Component
public class CaptureWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CaptureWebSocketHandler.class);

    private final VisionClientService visionClientService;
    private final ObjectMapper objectMapper;
    private final Map<String, StabilityCalculator> stabilityBySession = new ConcurrentHashMap<>();

    public CaptureWebSocketHandler(VisionClientService visionClientService, ObjectMapper objectMapper) {
        this.visionClientService = visionClientService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        stabilityBySession.put(session.getId(), new StabilityCalculator());
        log.info("event=capture_ws_connected wsSessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String sessionParam = extractQueryParam(session, "sessionId");
        MDC.put("sessionId", sessionParam == null ? "unknown" : sessionParam);
        try {
            CaptureFrameMessage payload = objectMapper.readValue(message.getPayload(), CaptureFrameMessage.class);
            if (payload.image() == null || payload.image().isBlank()) {
                sendError(session, "Campo 'image' ausente na mensagem.");
                return;
            }

            FrameAnalysisResult result = visionClientService.analyzeFrame(payload.image());

            StabilityCalculator stability = stabilityBySession.computeIfAbsent(session.getId(), id -> new StabilityCalculator());
            if (result.faceDetected() && result.boundingBox() != null) {
                double cx = result.boundingBox().x() + result.boundingBox().width() / 2.0;
                double cy = result.boundingBox().y() + result.boundingBox().height() / 2.0;
                stability.addSample(cx, cy);
            }

            CaptureFrameResponse response = new CaptureFrameResponse("frame_result", result, stability.currentStability());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (VisionServiceException ex) {
            sendError(session, ex.getMessage());
        } catch (Exception ex) {
            log.error("event=capture_ws_error message=\"{}\"", ex.getMessage(), ex);
            sendError(session, "Erro ao processar o frame.");
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        stabilityBySession.remove(session.getId());
        log.info("event=capture_ws_closed wsSessionId={} status={}", session.getId(), status);
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                new CaptureErrorResponse("error", message))));
    }

    private String extractQueryParam(WebSocketSession session, String key) {
        if (session.getUri() == null) {
            return null;
        }
        return UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst(key);
    }

    public record CaptureFrameMessage(String type, String image) {
    }

    public record CaptureFrameResponse(String type, FrameAnalysisResult analysis, double stability) {
    }

    public record CaptureErrorResponse(String type, String message) {
    }
}
