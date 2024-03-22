package org.debs.gc2023.dal.dto;

import com.google.gson.annotations.Expose;

public class PercentileResult {
    @Expose
    private double percentile;
    @Expose
    private long q1Latency;
    @Expose
    private long q2Latency;
    @Expose
    private long q1FailureLatency;
    @Expose
    private long q2FailureLatency;


    @Expose
    private long q1PostFailureLatency;
    @Expose
    private long q2PostFailureLatency;


    public PercentileResult(double percentile) {
        this.percentile = percentile;
        this.q1Latency = -1;
        this.q1FailureLatency = -1;
        this.q2Latency = -1;
        this.q2FailureLatency = -1;
    }

    public void setQ1Latency(long q1ValueAtPercentile) {
        this.q1Latency = q1ValueAtPercentile;
    }

    public void setQ2Latency(long q2ValueAtPercentile) {
        this.q2Latency = q2ValueAtPercentile;
    }

    public long getQ1Latency() {
        return q1Latency;
    }

    public long getQ2Latency() {
        return q2Latency;
    }

    public long getQ2FailureLatency() {
        return q2FailureLatency;
    }

    public void setQ2FailureLatency(long q2FailureLatency) {
        this.q2FailureLatency = q2FailureLatency;
    }

    public long getQ1FailureLatency() {
        return q1FailureLatency;
    }

    public void setQ1FailureLatency(long q1FailureLatency) {
        this.q1FailureLatency = q1FailureLatency;
    }

    public long getQ1PostFailureLatency() {
        return q1PostFailureLatency;
    }

    public void setQ1PostFailureLatency(long q1PostFailureLatency) {
        this.q1PostFailureLatency = q1PostFailureLatency;
    }

    public long getQ2PostFailureLatency() {
        return q2PostFailureLatency;
    }

    public void setQ2PostFailureLatency(long q2PostFailureLatency) {
        this.q2PostFailureLatency = q2PostFailureLatency;
    }

    public double getPercentile() {
        return percentile;
    }
}
