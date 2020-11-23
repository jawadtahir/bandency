package de.tum.i13.dal;

import de.tum.i13.challenger.LatencyMeasurement;

public class ToVerify {
    private BenchmarkDuration bd;
    private LatencyMeasurement lm;

    public ToVerify(LatencyMeasurement lm) {
        this.lm = lm;
    }

    public ToVerify(BenchmarkDuration bd) {
        this.bd = bd;
    }
}
