package com.biometric.capture.service;

import com.biometric.capture.domain.Metric;
import com.biometric.capture.domain.SessionResult;
import com.biometric.capture.dto.DashboardStatsDto;
import com.biometric.capture.dto.EvolutionPointDto;
import com.biometric.capture.dto.RankingEntryDto;
import com.biometric.capture.repository.MetricRepository;
import com.biometric.capture.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Aggregation lives here (not in SQL) so the failure-category thresholds stay in one
 * readable, unit-testable place. Mirrors the brightness/sharpness/framing thresholds used by
 * the vision-service (app/services/quality_metrics.py) so a "failure reason" means the same
 * thing on both sides.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final double BRIGHTNESS_LOW = 40.0;
    private static final double BRIGHTNESS_HIGH = 85.0;
    private static final double BLUR_LOW = 45.0;
    private static final double FACE_SIZE_TOO_FAR = 0.18;
    private static final double FACE_SIZE_TOO_CLOSE = 0.65;
    private static final double STABILITY_LOW = 60.0;

    private final SessionRepository sessionRepository;
    private final MetricRepository metricRepository;

    public DashboardService(SessionRepository sessionRepository, MetricRepository metricRepository) {
        this.sessionRepository = sessionRepository;
        this.metricRepository = metricRepository;
    }

    public DashboardStatsDto getStats() {
        long totalSessions = sessionRepository.count();
        List<Metric> completedMetrics = metricRepository.findAllForCompletedSessions();

        double averageScore = completedMetrics.stream()
                .mapToInt(m -> m.getSession().getScore())
                .average()
                .orElse(0.0);

        double averageDurationSeconds = completedMetrics.stream()
                .mapToLong(this::durationSeconds)
                .average()
                .orElse(0.0);

        Map<String, Long> failuresByCategory = buildFailureCategories(completedMetrics);
        List<EvolutionPointDto> evolution = buildEvolution(completedMetrics);

        return new DashboardStatsDto(
                totalSessions,
                round2(averageScore),
                round2(averageDurationSeconds),
                failuresByCategory,
                evolution
        );
    }

    public List<RankingEntryDto> getRanking() {
        return sessionRepository.findRanking().stream()
                .map(p -> new RankingEntryDto(p.getUserId(), p.getUserName(), round2(p.getAverageScore()), p.getSessionsCount()))
                .toList();
    }

    private long durationSeconds(Metric metric) {
        return ChronoUnit.SECONDS.between(metric.getSession().getCreatedAt(), metric.getCreatedAt());
    }

    private Map<String, Long> buildFailureCategories(List<Metric> completedMetrics) {
        Map<String, Long> categories = new LinkedHashMap<>();
        categories.put("brilho", 0L);
        categories.put("nitidez", 0L);
        categories.put("enquadramento", 0L);
        categories.put("distancia", 0L);
        categories.put("estabilidade", 0L);

        for (Metric metric : completedMetrics) {
            if (metric.getSession().getResult() != SessionResult.FAILED) {
                continue;
            }
            if (metric.getBrightness() < BRIGHTNESS_LOW || metric.getBrightness() > BRIGHTNESS_HIGH) {
                categories.merge("brilho", 1L, Long::sum);
            }
            if (metric.getBlur() < BLUR_LOW) {
                categories.merge("nitidez", 1L, Long::sum);
            }
            if (!metric.isCentered()) {
                categories.merge("enquadramento", 1L, Long::sum);
            }
            if (metric.getFaceSize() < FACE_SIZE_TOO_FAR || metric.getFaceSize() > FACE_SIZE_TOO_CLOSE) {
                categories.merge("distancia", 1L, Long::sum);
            }
            if (metric.getStability() < STABILITY_LOW) {
                categories.merge("estabilidade", 1L, Long::sum);
            }
        }
        return categories;
    }

    private List<EvolutionPointDto> buildEvolution(List<Metric> completedMetrics) {
        Map<LocalDate, List<Integer>> byDay = new TreeMap<>();
        for (Metric metric : completedMetrics) {
            LocalDate day = metric.getSession().getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
            byDay.computeIfAbsent(day, d -> new ArrayList<>()).add(metric.getSession().getScore());
        }
        return byDay.entrySet().stream()
                .map(e -> new EvolutionPointDto(
                        e.getKey(),
                        round2(e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0)),
                        e.getValue().size()
                ))
                .toList();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
