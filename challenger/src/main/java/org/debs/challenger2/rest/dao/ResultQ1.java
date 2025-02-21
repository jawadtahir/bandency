package org.debs.challenger2.rest.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResultQ1 {

    @JsonProperty("benchmark_id")
    private long benchmarkId;

    @JsonProperty("batch_seq_id")
    private long batchSeqId;

    @JsonProperty("entries")
    private List<Anomaly> anomalies;


    public long getBenchmarkId() {
        return benchmarkId;
    }

    public void setBenchmarkId(long benchmarkId) {
        this.benchmarkId = benchmarkId;
    }

    public long getBatchSeqId() {
        return batchSeqId;
    }

    public void setBatchSeqId(long batchSeqId) {
        this.batchSeqId = batchSeqId;
    }

    public List<Anomaly> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<Anomaly> anomalies) {
        this.anomalies = anomalies;
    }
}
