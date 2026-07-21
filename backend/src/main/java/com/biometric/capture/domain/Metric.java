package com.biometric.capture.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "metrics")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;

    @Column(nullable = false)
    private double brightness;

    @Column(nullable = false)
    private double blur;

    @Column(name = "face_size", nullable = false)
    private double faceSize;

    @Column(nullable = false)
    private boolean centered;

    @Column(nullable = false)
    private double stability;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Metric() {
    }

    public Metric(Session session, double brightness, double blur, double faceSize, boolean centered, double stability) {
        this.session = session;
        this.brightness = brightness;
        this.blur = blur;
        this.faceSize = faceSize;
        this.centered = centered;
        this.stability = stability;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public double getBrightness() {
        return brightness;
    }

    public double getBlur() {
        return blur;
    }

    public double getFaceSize() {
        return faceSize;
    }

    public boolean isCentered() {
        return centered;
    }

    public double getStability() {
        return stability;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
