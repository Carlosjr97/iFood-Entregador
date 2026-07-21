package com.biometric.capture.service;

import com.biometric.capture.domain.Metric;
import com.biometric.capture.domain.Session;
import com.biometric.capture.domain.SessionResult;
import com.biometric.capture.domain.User;
import com.biometric.capture.dto.DashboardStatsDto;
import com.biometric.capture.repository.MetricRepository;
import com.biometric.capture.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    SessionRepository sessionRepository;
    @Mock
    MetricRepository metricRepository;

    @Test
    void getStats_computesAverageScoreAndFailureCategoriesFromCompletedSessionsOnly() throws Exception {
        DashboardService dashboardService = new DashboardService(sessionRepository, metricRepository);

        User user = new User("Ana", "ana@example.com");
        setId(user, 1L);

        Session passedSession = new Session(user);
        setId(passedSession, 1L);
        passedSession.setScore(90);
        passedSession.setResult(SessionResult.PASSED);

        Session failedSession = new Session(user);
        setId(failedSession, 2L);
        failedSession.setScore(30);
        failedSession.setResult(SessionResult.FAILED);

        Metric passedMetric = new Metric(passedSession, 60.0, 90.0, 0.35, true, 95.0);
        Metric failedMetric = new Metric(failedSession, 20.0, 30.0, 0.10, false, 40.0);

        when(sessionRepository.count()).thenReturn(2L);
        when(metricRepository.findAllForCompletedSessions()).thenReturn(List.of(passedMetric, failedMetric));

        DashboardStatsDto stats = dashboardService.getStats();

        assertThat(stats.totalSessions()).isEqualTo(2L);
        assertThat(stats.averageScore()).isEqualTo(60.0);
        assertThat(stats.failuresByCategory().get("brilho")).isEqualTo(1L);
        assertThat(stats.failuresByCategory().get("nitidez")).isEqualTo(1L);
        assertThat(stats.failuresByCategory().get("enquadramento")).isEqualTo(1L);
        assertThat(stats.failuresByCategory().get("distancia")).isEqualTo(1L);
        assertThat(stats.failuresByCategory().get("estabilidade")).isEqualTo(1L);
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
