package org.debs.gc2023.dal;

import org.debs.gc2023.challenger.LatencyMeasurement;

public class ToVerify {
    private BenchmarkDuration bd;
    private LatencyMeasurement lm;
    private VerificationType type;

    public ToVerify(LatencyMeasurement lm) {
        this.type = VerificationType.Measurement;
        this.lm = lm;
    }

    public ToVerify(BenchmarkDuration bd) {
        this.type = VerificationType.Duration;
        this.bd = bd;
    }

    public VerificationType getType() {
        return type;
    }

    public void setType(VerificationType type) {
        this.type = type;
    }

    public LatencyMeasurement getLatencyMeasurement() {
        return lm;
    }

    public BenchmarkDuration getBenchmarkDuration() {
        return bd;
    }
}
