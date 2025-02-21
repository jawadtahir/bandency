package org.debs.challenger2.rest.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BatchRequest {

    @JsonProperty("apitoken")
    private String token;
    @JsonProperty("test")
    private boolean test;
    @JsonProperty("name")
    private String benchmarkName;

    public BatchRequest(){}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }
}
