package org.debs.challenger2.benchmark;

import org.HdrHistogram.Histogram;

import java.util.concurrent.ConcurrentHashMap;

public class BenchmarkDuration {
    private final String benchmarkId;
    private final long startTime;
    private final long endTime;
    private final ConcurrentHashMap<Integer, Histogram> histograms;



    public BenchmarkDuration(String benchmarkId, long startTime, long endTime,
            ConcurrentHashMap<Integer, Histogram> histograms) {

        this.benchmarkId = benchmarkId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.histograms = histograms;
    }

    public String getBenchmarkId() {
        return benchmarkId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public ConcurrentHashMap<Integer, Histogram> getHistograms() {
        return histograms;
    }
}
