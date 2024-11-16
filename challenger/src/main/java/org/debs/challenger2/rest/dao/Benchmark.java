package org.debs.challenger2.rest.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Benchmark {

    @JsonProperty("benchmarkId")
    private String benchmarkId;

    public Benchmark(String id){
        this.benchmarkId = id;
    }
}
