package de.tum.i13.dal;

public class BenchmarkDuration {
    private final long benchmarkId;
    private final long startTime;
    private final long endTime;
    private final double averageLatency;

    public BenchmarkDuration(long benchmarkId, long startTime, long endTime, double averageLatency) {

        this.benchmarkId = benchmarkId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.averageLatency = averageLatency;
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
}
