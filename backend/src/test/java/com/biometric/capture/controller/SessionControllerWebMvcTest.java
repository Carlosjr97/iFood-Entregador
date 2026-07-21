package com.biometric.capture.controller;

import com.biometric.capture.domain.SessionResult;
import com.biometric.capture.dto.CreateSessionRequest;
import com.biometric.capture.dto.SessionDto;
import com.biometric.capture.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
class SessionControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    SessionService sessionService;

    @Test
    void createSession_returns201WithSessionBody() throws Exception {
        SessionDto dto = new SessionDto(1L, 5L, "Ana", 0, SessionResult.PENDING, Instant.now());
        when(sessionService.createSession(any())).thenReturn(dto);

        mockMvc.perform(post("/api/sessions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateSessionRequest(5L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.result").value("PENDING"));
    }

    @Test
    void createSession_rejectsMissingUserIdWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
