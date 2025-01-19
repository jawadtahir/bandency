package org.debs.challenger2.rest.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultResponse {

    @JsonProperty("bench_id")
    private String benchmarkId;
    @JsonProperty("batch_number")
    private Long batchSeqId;
    @JsonProperty("query")
    private Integer query;
    @JsonProperty("timestamp")
    private Long timestamp;

    public ResultResponse(String benchmarkId, Long batchSeqId, Integer query, Long timestamp) {
        this.benchmarkId = benchmarkId;
        this.batchSeqId = batchSeqId;
        this.query = query;
        this.timestamp = timestamp;
    }

    public String getBenchmarkId() {
        return benchmarkId;
    }

    public void setBenchmarkId(String benchmarkId) {
        this.benchmarkId = benchmarkId;
    }

    public Long getBatchSeqId() {
        return batchSeqId;
    }

    public void setBatchSeqId(Long batchSeqId) {
        this.batchSeqId = batchSeqId;
    }

    public Integer getQuery() {
        return query;
    }

    public void setQuery(Integer query) {
        this.query = query;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
