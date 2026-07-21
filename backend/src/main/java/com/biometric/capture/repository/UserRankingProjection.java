package com.biometric.capture.repository;

public interface UserRankingProjection {
    Long getUserId();

    String getUserName();

    Double getAverageScore();

    Long getSessionsCount();
}
