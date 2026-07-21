package com.biometric.capture.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StabilityCalculatorTest {

    @Test
    void defaultStabilityIsHighBeforeEnoughSamples() {
        StabilityCalculator calculator = new StabilityCalculator();
        calculator.addSample(0.5, 0.5);
        assertThat(calculator.currentStability()).isEqualTo(100.0);
    }

    @Test
    void stableSamplesYieldHighStability() {
        StabilityCalculator calculator = new StabilityCalculator();
        for (int i = 0; i < 8; i++) {
            calculator.addSample(0.5, 0.5);
        }
        assertThat(calculator.currentStability()).isEqualTo(100.0);
    }

    @Test
    void jitterySamplesYieldLowerStability() {
        StabilityCalculator calculator = new StabilityCalculator();
        double[] xs = {0.2, 0.8, 0.1, 0.9, 0.15, 0.85, 0.2, 0.8};
        for (double x : xs) {
            calculator.addSample(x, 0.5);
        }
        assertThat(calculator.currentStability()).isLessThan(50.0);
    }
}
