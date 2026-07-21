package com.biometric.capture.repository;

import com.biometric.capture.domain.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {

    @Query("select m from Metric m join fetch m.session s join fetch s.user where s.result <> com.biometric.capture.domain.SessionResult.PENDING")
    List<Metric> findAllForCompletedSessions();
}
