package de.tum.i13.challenger;

import de.tum.i13.bandency.Batch;
import de.tum.i13.bandency.Result;
import de.tum.i13.datasets.airquality.AirQualityDataSource;

import java.util.ArrayList;
import java.util.HashMap;

public class BenchmarkState {
    private String token;
    private int batchSize;
    private HashMap<Long, Long> pingCorrelation;
    private ArrayList<Long> measurements;

    private HashMap<Long, Long> requestCorrelation;
    private ArrayList<Long> requestMeasurements;

    private double averageLatency;
    private long startNanoTime;
    private AirQualityDataSource datasource;

    public BenchmarkState() {
        this.averageLatency = 0.0;
        this.batchSize = -1;
        pingCorrelation = new HashMap<>();
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
        pingCorrelation.put(random_id, nanoTime);
    }

    public void correlatePing(long correlation_id, long nanoTime) {
        if(pingCorrelation.containsKey(correlation_id)) {
            Long sentTime = pingCorrelation.get(correlation_id);
            pingCorrelation.remove(correlation_id);
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

    public Batch getNextBatch() {
        Batch batch = this.datasource.nextElement();
        this.requestCorrelation.put(batch.getSeqId(), System.nanoTime());
        return batch;
    }

    public void processed(Result request, long nanoTime) {
        if(pingCorrelation.containsKey(request.getPayloadSeqId())) {
            Long sentTime = pingCorrelation.get(request.getPayloadSeqId());
            pingCorrelation.remove(request.getPayloadSeqId());
            long duration = nanoTime - sentTime;
            this.requestMeasurements.add(duration);
        }
    }
}
