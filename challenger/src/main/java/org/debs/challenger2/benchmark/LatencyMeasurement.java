package org.debs.challenger2.benchmark;


import org.bson.types.ObjectId;
import org.debs.challenger2.rest.dao.ResultQ1;
import org.debs.challenger2.rest.dao.ResultQ2;

// TODO; update this class accordingly
public class LatencyMeasurement {
    private final ObjectId benchmarkId;
    private final Long batchId;

    private Integer query;
    private final Long startTime;

    private Long endTime;


    public LatencyMeasurement(ObjectId benchmarkId, Long batchId, Long startTime) {
        this.benchmarkId = benchmarkId;
        this.batchId = batchId;
        this.startTime = startTime;
    }

    public void markEnd(Integer query, long endTime){
        this.query = query;
        this.endTime = endTime;
    }

    public ObjectId getBenchmarkId() {
        return benchmarkId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public Integer getQuery() {
        return query;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}

