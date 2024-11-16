package org.debs.challenger2.rest.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BenchmarkStart {

    @JsonProperty("benchmark_id")
    private String benchmarkId;

    @JsonProperty("start_time")
    private Long startTime;

    public BenchmarkStart(String benchmarkId, Long startTime){
        this.benchmarkId = benchmarkId;
        this.startTime = startTime;
    }

    public String getBenchmarkId() {
        return benchmarkId;
    }

    public void setBenchmarkId(String benchmarkId) {
        this.benchmarkId = benchmarkId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
}
