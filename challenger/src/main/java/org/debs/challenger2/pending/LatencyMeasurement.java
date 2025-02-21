package org.debs.challenger2.pending;


import org.bson.types.ObjectId;
import org.debs.challenger2.db.IQueries;

// TODO; update this class accordingly
public class LatencyMeasurement implements IPendingTask {
    private final ObjectId groupId;
    private final ObjectId benchmarkId;

    private final Long batchId;

    private Integer query;
    private final Long startTime;

    private Long endTime;


    public LatencyMeasurement(ObjectId groupId, ObjectId benchmarkId, Long batchId, Long startTime) {
        this.groupId = groupId;
        this.benchmarkId = benchmarkId;
        this.batchId = batchId;
        this.startTime = startTime;
    }

    public void markEnd(Integer query, long endTime){
        this.query = query;
        this.endTime = endTime;
    }

    public ObjectId getGroupId() {
        return groupId;
    }

    public void setQuery(Integer query) {
        this.query = query;
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

    @Override
    public void doPending(IQueries queries) {

        Long latency = getEndTime() - getStartTime();

        queries.insertLatency(getGroupId(), getQuery(), latency);
    }
}

