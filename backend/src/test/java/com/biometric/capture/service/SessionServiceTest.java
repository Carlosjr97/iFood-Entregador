package com.biometric.capture.service;

import com.biometric.capture.domain.Session;
import com.biometric.capture.domain.SessionResult;
import com.biometric.capture.domain.User;
import com.biometric.capture.dto.CompleteSessionRequest;
import com.biometric.capture.dto.CreateSessionRequest;
import com.biometric.capture.dto.SessionDto;
import com.biometric.capture.repository.MetricRepository;
import com.biometric.capture.repository.SessionRepository;
import com.biometric.capture.websocket.DashboardWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    SessionRepository sessionRepository;
    @Mock
    MetricRepository metricRepository;
    @Mock
    UserService userService;
    @Mock
    DashboardWebSocketHandler dashboardWebSocketHandler;

    SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(sessionRepository, metricRepository, userService, dashboardWebSocketHandler);
    }

    @Test
    void createSession_persistsPendingSessionForExistingUser() {
        User user = newUser(1L, "Ana", "ana@example.com");
        when(userService.findUserOrThrow(1L)).thenReturn(user);
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> {
            Session session = invocation.getArgument(0);
            setId(session, 10L);
            return session;
        });

        SessionDto dto = sessionService.createSession(new CreateSessionRequest(1L));

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.result()).isEqualTo(SessionResult.PENDING);
        assertThat(dto.score()).isZero();
    }

    @Test
    void completeSession_marksPassedWhenScoreAboveThresholdAndLivenessCompleted() {
        User user = newUser(1L, "Ana", "ana@example.com");
        Session session = new Session(user);
        setId(session, 10L);
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionDto dto = sessionService.completeSession(10L,
                new CompleteSessionRequest(85, 60.0, 90.0, 0.35, true, 95.0, true));

        assertThat(dto.result()).isEqualTo(SessionResult.PASSED);
        verify(metricRepository).save(any());
        verify(dashboardWebSocketHandler).broadcast(any());
    }

    @Test
    void completeSession_marksFailedWhenLivenessNotCompletedDespiteGoodScore() {
        User user = newUser(1L, "Ana", "ana@example.com");
        Session session = new Session(user);
        setId(session, 11L);
        when(sessionRepository.findById(11L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionDto dto = sessionService.completeSession(11L,
                new CompleteSessionRequest(90, 60.0, 90.0, 0.35, true, 95.0, false));

        assertThat(dto.result()).isEqualTo(SessionResult.FAILED);
    }

    @Test
    void completeSession_marksFailedWhenScoreBelowThreshold() {
        User user = newUser(1L, "Ana", "ana@example.com");
        Session session = new Session(user);
        setId(session, 12L);
        when(sessionRepository.findById(12L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionDto dto = sessionService.completeSession(12L,
                new CompleteSessionRequest(40, 20.0, 30.0, 0.10, false, 40.0, true));

        assertThat(dto.result()).isEqualTo(SessionResult.FAILED);
    }

    private User newUser(Long id, String name, String email) {
        User user = new User(name, email);
        setId(user, id);
        return user;
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
