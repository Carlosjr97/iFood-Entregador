package com.biometric.capture.dto;

import java.time.Instant;

public record UserDto(Long id, String name, String email, Instant createdAt) {
}
