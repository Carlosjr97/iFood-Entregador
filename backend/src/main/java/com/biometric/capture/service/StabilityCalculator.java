package com.biometric.capture.service;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks recent face-center positions (relative to the frame) received during a capture
 * session and turns their jitter into a 0-100 "estabilidade" score. This is deliberately a
 * plain, per-session POJO (not a Spring bean) — the WebSocket handler owns one instance per
 * connection.
 */
public class StabilityCalculator {

    private static final int WINDOW_SIZE = 8;
    private static final double JITTER_SENSITIVITY = 400.0;

    private final Deque<double[]> samples = new ArrayDeque<>();

    public void addSample(double centerX, double centerY) {
        samples.addLast(new double[]{centerX, centerY});
        while (samples.size() > WINDOW_SIZE) {
            samples.removeFirst();
        }
    }

    public double currentStability() {
        if (samples.size() < 2) {
            return 100.0;
        }
        double meanX = samples.stream().mapToDouble(s -> s[0]).average().orElse(0);
        double meanY = samples.stream().mapToDouble(s -> s[1]).average().orElse(0);
        double variance = samples.stream()
                .mapToDouble(s -> Math.pow(s[0] - meanX, 2) + Math.pow(s[1] - meanY, 2))
                .average()
                .orElse(0);
        double jitter = Math.sqrt(variance);
        double score = 100.0 - (jitter * JITTER_SENSITIVITY);
        return Math.max(0.0, Math.min(100.0, score));
    }
}
