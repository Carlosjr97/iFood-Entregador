package com.biometric.capture.controller;

import com.biometric.capture.dto.CompleteSessionRequest;
import com.biometric.capture.dto.CreateSessionRequest;
import com.biometric.capture.dto.SessionDto;
import com.biometric.capture.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto createSession(@Valid @RequestBody CreateSessionRequest request) {
        return sessionService.createSession(request);
    }

    @PostMapping("/{id}/complete")
    public SessionDto completeSession(@PathVariable Long id, @Valid @RequestBody CompleteSessionRequest request) {
        return sessionService.completeSession(id, request);
    }

    @GetMapping("/{id}")
    public SessionDto getSession(@PathVariable Long id) {
        return sessionService.getSession(id);
    }

    @GetMapping
    public List<SessionDto> listSessions(@RequestParam(required = false) Long userId) {
        return userId != null ? sessionService.listSessionsForUser(userId) : sessionService.listAllSessions();
    }
}
