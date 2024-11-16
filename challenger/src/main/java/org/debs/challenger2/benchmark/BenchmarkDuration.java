package org.debs.challenger2.benchmark;

import org.HdrHistogram.Histogram;

public class BenchmarkDuration {
    private final long benchmarkId;
    private final long startTime;
    private final long endPrefailureNanoTime;
    private final long startFailureTime;
    private final long endFailureTime;
    private final long startPostfailureTime;
    private final long endTime;
    private final double averageLatency;
    private Histogram q1PostFailureHistogram;
    private Histogram q1Histogram;
    private Histogram q1FailureHistogram;
    private Histogram q2Histogram;
    private Histogram q2PostFailureHistogram;
    private Histogram q2FailureHistogram;
    private final boolean q1Active;
    private final boolean q2Active;
    public final boolean failureActive;


    public BenchmarkDuration(long benchmarkId, long startTime, long endTime, double averageLatency,
            Histogram q1Histogram, Histogram q2Histogram, boolean q1Active, boolean q2Active) {

        this.benchmarkId = benchmarkId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.averageLatency = averageLatency;
        this.q1Histogram = q1Histogram;
        this.q2Histogram = q2Histogram;
        this.q1Active = q1Active;
        this.q2Active = q2Active;
        this.failureActive = false;
        this.endPrefailureNanoTime = -1;
        this.startFailureTime = -1;
        this.endFailureTime = -1;
        this.startPostfailureTime = -1;
    }

    public BenchmarkDuration(long benchmarkId, long startTime, long endPrefailuretime,
            long startFailureTime, long endFailureTime, long startPostfailureTime, long endTime,
            double averageLatency, Histogram q1Histogram, Histogram q1FailureHistogram,
            Histogram q1PostFailureHistogram, Histogram q2Histogram, Histogram q2FailureHistogram,
            Histogram q2PostFailureHistogram, boolean q1Active, boolean q2Active) {

        this.benchmarkId = benchmarkId;
        this.startTime = startTime;
        this.endPrefailureNanoTime = endPrefailuretime;
        this.startFailureTime = startFailureTime;
        this.endFailureTime = endFailureTime;
        this.startPostfailureTime = startPostfailureTime;
        this.endTime = endTime;
        this.averageLatency = averageLatency;
        this.q1Histogram = q1Histogram;
        this.q1FailureHistogram = q1FailureHistogram;
        this.q1PostFailureHistogram = q1PostFailureHistogram;
        this.q2Histogram = q2Histogram;
        this.q2FailureHistogram = q2FailureHistogram;
        this.q2PostFailureHistogram = q2PostFailureHistogram;
        this.q1Active = q1Active;
        this.q2Active = q2Active;
        this.failureActive = true;
    }

    public boolean isFailureActive() {
        return failureActive;
    }

    public long getEndPrefailureNanoTime() {
        return endPrefailureNanoTime;
    }

    public long getStartFailureTime() {
        return startFailureTime;
    }

    public long getEndFailureTime() {
        return endFailureTime;
    }

    public long getStartPostfailureTime() {
        return startPostfailureTime;
    }

    public long getBenchmarkId() {
        return benchmarkId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public Histogram getQ2Histogram() {
        return q2Histogram;
    }

    public Histogram getQ2FailureHistogram() {
        return q2FailureHistogram;
    }

    public Histogram getQ2PostFailureHistogram() {
        return q2PostFailureHistogram;
    }

    public Histogram getQ1Histogram() {
        return q1Histogram;
    }

    public Histogram getQ1FailureHistogram() {
        return q1FailureHistogram;
    }

    public Histogram getQ1PostFailureHistogram() {
        return q1PostFailureHistogram;
    }

    public boolean isQ1Active() {
        return q1Active;
    }

    public boolean isQ2Active() {
        return q2Active;
    }
}
