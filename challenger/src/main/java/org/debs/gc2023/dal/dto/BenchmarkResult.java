package org.debs.gc2023.dal.dto;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class BenchmarkResult {


    @Expose
    private long benchmarkId;
    @Expose
    private ArrayList<PercentileResult> percentileResults;
    @Expose
    private long q1_count;
    @Expose
    private long q1_Failure_count;
    @Expose
    private long q1_Post_Failure_count;
    @Expose
    private long q2_count;
    @Expose
    private long q2_Failure_count;
    @Expose
    private long q2_Post_Failure_count;
    @Expose
    private double seconds;
    @Expose
    private double q1Throughput;
    @Expose
    private double q2Throughput;

    public double getQ1FailureThroughput() {
        return q1_Failure_Throughput;
    }

    @Expose
    private double q1_Failure_Throughput;

    public double getQ2PostFailureThroughput() {
        return q2_Post_Failure_Throughput;
    }

    @Expose
    private double q2_Post_Failure_Throughput;

    public double getQ1PostFailureThroughput() {
        return q1_Post_Failure_Throughput;
    }

    @Expose
    private double q1_Post_Failure_Throughput;

    public double getQ2FailureThroughput() {
        return q2_Failure_Throughput;
    }

    @Expose
    private double q2_Failure_Throughput;

    @Expose
    private double q1_90Percentile;
    @Expose
    private double q1_Failure90Percentile;
    @Expose
    private double q1_PostFailure90Percentile;
    @Expose
    private double q2_90Percentile;
    @Expose
    private double q2_Failure90Percentile;
    @Expose
    private double q2_PostFailure90Percentile;

    public BenchmarkResult(long benchmarkId, ArrayList<PercentileResult> percentileResults,
            long q1_count, long q1_Failure_count, long q1_Post_Failure_count, long q2_count,
            long q2_Failure_count, long q2_Post_Failure_count, double seconds, double q1Throughput,
            double q1_Failure_Throughput, double q1_Post_Failure_Throughput, double q2Throughput,
            double q2_Failure_Throughput, double q2_Post_Failure_Throughput, double q1_90Percentile,
            double q1_Failure90Percentile, double q1_PostFailure90Percentile,
            double q2_90Percentile, double q2_Failure90Percentile,
            double q2_PostFailure90Percentile) {

        this.benchmarkId = benchmarkId;
        this.percentileResults = percentileResults;
        this.q1_count = q1_count;
        this.q1_Failure_count = q1_Failure_count;
        this.q1_Post_Failure_count = q1_Post_Failure_count;
        this.q2_count = q2_count;
        this.q2_Failure_count = q1_Failure_count;
        this.q2_Post_Failure_count = q1_Post_Failure_count;
        this.seconds = seconds;
        this.q1Throughput = q1Throughput;
        this.q1_Failure_Throughput = q1_Failure_Throughput;
        this.q1_Post_Failure_Throughput = q1_Post_Failure_Throughput;
        this.q2Throughput = q2Throughput;
        this.q2_Failure_Throughput = q2_Failure_Throughput;
        this.q2_Post_Failure_Throughput = q2_Post_Failure_Throughput;
        this.q1_90Percentile = q1_90Percentile;
        this.q1_Failure90Percentile = q1_Failure90Percentile;
        this.q1_PostFailure90Percentile = q1_PostFailure90Percentile;
        this.q2_90Percentile = q2_90Percentile;
        this.q2_Failure90Percentile = q2_Failure90Percentile;
        this.q2_PostFailure90Percentile = q2_PostFailure90Percentile;

    }

    public BenchmarkResult(long benchmarkId, ArrayList<PercentileResult> percentileResults,
            long q1_count, long q2_count, double seconds, double q1Throughput, double q2Throughput,
            double q1_90Percentile, double q2_90Percentile) {

        this.benchmarkId = benchmarkId;
        this.percentileResults = percentileResults;
        this.q1_count = q1_count;
        this.q2_count = q2_count;
        this.seconds = seconds;
        this.q1Throughput = q1Throughput;
        this.q2Throughput = q2Throughput;
        this.q1_90Percentile = q1_90Percentile;
        this.q2_90Percentile = q2_90Percentile;
    }

    public ArrayList<PercentileResult> getPercentileResults() {
        return percentileResults;
    }

    public long getQ1_count() {
        return q1_count;
    }

    public long getQ1_Failure_count() {
        return q1_Failure_count;
    }

    public long getQ1_Post_Failure_count() {
        return q1_Post_Failure_count;
    }

    public long getQ2_count() {
        return q2_count;
    }

    public long getQ2_Failure_count() {
        return q2_Failure_count;
    }

    public long getQ2_Post_Failure_count() {
        return q2_Post_Failure_count;
    }

    public double getSeconds() {
        return seconds;
    }

    public double getQ1Throughput() {
        return q1Throughput;
    }

    public double getQ2Throughput() {
        return q2Throughput;
    }

    public double getQ1_90Percentile() {
        return q1_90Percentile;
    }

    public double getQ1_Failure90Percentile() {
        return q1_Failure90Percentile;
    }

    public double getQ1_PostFailure90Percentile() {
        return q1_PostFailure90Percentile;
    }

    public double getQ2_90Percentile() {
        return q2_90Percentile;
    }

    public double getQ2_Failure90Percentile() {
        return q2_PostFailure90Percentile;
    }

    public double getQ2_PostFailure90Percentile() {
        return q2_PostFailure90Percentile;
    }

    public long getBenchmarkId() {
        return benchmarkId;
    }
}
