package de.tum.i13.challenger;

import de.tum.i13.datasets.airquality.AirQualityDataSource;

import java.util.ArrayList;
import java.util.HashMap;

public class BenchmarkState {
    private String token;
    private int batchSize;
    private HashMap<Long, Long> lm;
    private ArrayList<Long> measurements;
    private double averageLatency;
    private long startNanoTime;
    private AirQualityDataSource datasource;

    public BenchmarkState() {
        this.averageLatency = 0.0;
        this.batchSize = -1;
        lm = new HashMap<>();
        this.measurements = new ArrayList<>();

        averageLatency = 0.0;
        startNanoTime = 0;
        datasource = null;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public String getToken() {
        return token;
    }

    public void addLatencyTimeStamp(long random_id, long nanoTime) {
        lm.put(random_id, nanoTime);
    }

    public void correlatePing(long correlation_id, long nanoTime) {
        if(lm.containsKey(correlation_id)) {
            Long sentTime = lm.get(correlation_id);
            long duration = nanoTime - sentTime;
            this.measurements.add(duration);
        }
    }

    public void calcAverageTransportLatency() {
        if(this.measurements.size() > 0) {
            this.averageLatency = this.measurements.stream().mapToLong(a -> a).average().getAsDouble();
        }
    }

    public void startBenchmark(long startNanoTime) {
        this.startNanoTime = startNanoTime;
    }

    public void setDatasource(AirQualityDataSource newDataSource) {
        this.datasource = newDataSource;
    }

    public AirQualityDataSource getDatasource() {
        return this.datasource;
    }
}
