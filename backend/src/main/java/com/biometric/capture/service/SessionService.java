package com.biometric.capture.service;

import com.biometric.capture.domain.Metric;
import com.biometric.capture.domain.Session;
import com.biometric.capture.domain.SessionResult;
import com.biometric.capture.domain.User;
import com.biometric.capture.dto.CompleteSessionRequest;
import com.biometric.capture.dto.CreateSessionRequest;
import com.biometric.capture.dto.DashboardEventDto;
import com.biometric.capture.dto.SessionDto;
import com.biometric.capture.exception.ResourceNotFoundException;
import com.biometric.capture.repository.MetricRepository;
import com.biometric.capture.repository.SessionRepository;
import com.biometric.capture.websocket.DashboardWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final int PASS_SCORE_THRESHOLD = 70;

    private final SessionRepository sessionRepository;
    private final MetricRepository metricRepository;
    private final UserService userService;
    private final DashboardWebSocketHandler dashboardWebSocketHandler;

    public SessionService(SessionRepository sessionRepository,
                           MetricRepository metricRepository,
                           UserService userService,
                           DashboardWebSocketHandler dashboardWebSocketHandler) {
        this.sessionRepository = sessionRepository;
        this.metricRepository = metricRepository;
        this.userService = userService;
        this.dashboardWebSocketHandler = dashboardWebSocketHandler;
    }

    public SessionDto createSession(CreateSessionRequest request) {
        User user = userService.findUserOrThrow(request.userId());
        Session session = sessionRepository.save(new Session(user));
        MDC.put("sessionId", String.valueOf(session.getId()));
        try {
            log.info("event=session_created userId={}", user.getId());
        } finally {
            MDC.clear();
        }
        return toDto(session);
    }

    public SessionDto completeSession(Long sessionId, CompleteSessionRequest request) {
        Session session = findSessionOrThrow(sessionId);
        boolean passed = request.score() >= PASS_SCORE_THRESHOLD && Boolean.TRUE.equals(request.livenessCompleted());

        session.setScore(request.score());
        session.setResult(passed ? SessionResult.PASSED : SessionResult.FAILED);
        sessionRepository.save(session);

        Metric metric = new Metric(
                session,
                request.brightness(),
                request.blur(),
                request.faceSize(),
                request.centered(),
                request.stability()
        );
        metricRepository.save(metric);

        MDC.put("sessionId", String.valueOf(sessionId));
        try {
            log.info("event=session_completed score={} result={}", request.score(), session.getResult());
        } finally {
            MDC.clear();
        }

        dashboardWebSocketHandler.broadcast(
                new DashboardEventDto("session_completed", session.getId(), session.getScore(), session.getResult().name())
        );

        return toDto(session);
    }

    @Transactional(readOnly = true)
    public SessionDto getSession(Long id) {
        return toDto(findSessionOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<SessionDto> listSessionsForUser(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SessionDto> listAllSessions() {
        return sessionRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    private Session findSessionOrThrow(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada: " + id));
    }

    private SessionDto toDto(Session session) {
        return new SessionDto(
                session.getId(),
                session.getUser().getId(),
                session.getUser().getName(),
                session.getScore(),
                session.getResult(),
                session.getCreatedAt()
        );
    }
}
