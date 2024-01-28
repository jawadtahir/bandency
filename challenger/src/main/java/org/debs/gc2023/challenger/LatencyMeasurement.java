package org.debs.gc2023.challenger;

import org.debs.gc2023.bandency.ResultQ1;
import org.debs.gc2023.bandency.ResultQ2;

// TODO; update this class accordingly
public class LatencyMeasurement {
    private final Long benchmarkId;
    private final Long batchId;
    private boolean hasQ1FailureResults;
    private boolean hasQ1PostFailureResults;

    public Long getStartTime() {
        return startTime;
    }

    private final Long startTime;

    private long q1ResultTime;

    public long getQ1FailureResultTime() {
        return q1FailureResultTime;
    }

    private long q1FailureResultTime;

    public long getQ1PostFailureResultTime() {
        return q1PostFailureResultTime;
    }


    private long q1PostFailureResultTime;
    private ResultQ1 q1Payload;
    private boolean hasQ1Results;
    private boolean hasQ2Results;
    private boolean hasQ2FailureResults;
    private boolean hasQ2PostFailureResults;
    private boolean hasFailureResults;
    private long q2ResultTime;

    public long getQ2FailureResultTime() {
        return q2FailureResultTime;
    }

    private long q2FailureResultTime;

    public long getQ2PostFailureResultTime() {
        return q2PostFailureResultTime;
    }

    private long q2PostFailureResultTime;
    private ResultQ2 q2Payload;

    public LatencyMeasurement(Long benchmarkId, Long batchId, Long startTime) {
        this.benchmarkId = benchmarkId;
        this.batchId = batchId;
        this.startTime = startTime;

        this.hasQ1Results = false;
        this.hasQ2Results = false;
        this.hasQ1FailureResults = false;
        this.hasQ1PostFailureResults = false;
        this.hasQ2FailureResults = false;
        this.hasQ2PostFailureResults = false;
        this.hasFailureResults = false;
    }

    public void setQ1Results(long q1ResultTime, ResultQ1 payload) {

        this.q1ResultTime = q1ResultTime;
        this.q1Payload = payload;
        this.hasQ1Results = true;
    }

    public long getQ1ResultTime() {
        return q1ResultTime;
    }

    public ResultQ1 getQ1Payload() {
        return q1Payload;
    }

    public boolean hasQ1Results() {
        return this.hasQ1Results;
    }

    public boolean hasQ1FailureResults() {
        return this.hasQ1FailureResults;
    }

    public boolean hasQ1PostFailureResults() {
        return this.hasQ1PostFailureResults;
    }


    public boolean hasQ2FailureResults() {
        return this.hasQ2FailureResults;
    }

    public boolean hasQ2PostFailureResults() {
        return this.hasQ2PostFailureResults;
    }


    public boolean hasQ2Results() {
        return this.hasQ2Results;
    }

    public void setQ2Results(long q2Resulttime, ResultQ2 payload) {

        this.q2ResultTime = q2Resulttime;
        this.q2Payload = payload;

        this.hasQ2Results = true;
    }

    public long getQ2ResultTime() {
        return q2ResultTime;
    }

    public Long getBenchmarkId() {
        return benchmarkId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public boolean hasFailureResults() {
        return this.hasFailureResults;
    }

    public void setQ1FailureResults(long q1ResultTime, ResultQ1 payload) {
        this.q1FailureResultTime = q1ResultTime;
        this.q1Payload = payload;
        this.hasFailureResults = true;
        this.hasQ1Results = true;
        this.hasQ1FailureResults = true;
    }

    public void setQ1PostFailureResults(long q1ResultTime, ResultQ1 payload) {
        this.q1PostFailureResultTime = q1ResultTime;
        this.q1Payload = payload;
        this.hasFailureResults = true;
        this.hasQ1Results = true;
        this.hasQ1PostFailureResults = true;
    }

    public void setQ2FailureResults(long q2ResultTime, ResultQ2 payload) {
        this.q2FailureResultTime = q2ResultTime;
        this.q2Payload = payload;
        this.hasQ2Results = true;
        this.hasFailureResults = true;
        this.hasQ2FailureResults = true;
    }

    public void setQ2PostFailureResults(long q2ResultTime, ResultQ2 payload) {
        this.q2PostFailureResultTime = q2ResultTime;
        this.hasFailureResults = true;
        this.q2Payload = payload;
        this.hasQ2Results = true;
        this.hasQ2PostFailureResults = true;
    }

}

