package de.tum.i13.challenger;

import de.tum.i13.bandency.Batch;
import de.tum.i13.bandency.ResultQ1;
import de.tum.i13.bandency.ResultQ2;
import de.tum.i13.dal.BenchmarkDuration;
import de.tum.i13.dal.ToVerify;
import de.tum.i13.datasets.airquality.AirQualityDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class BenchmarkState {
    private final ArrayBlockingQueue<ToVerify> dbInserter;
    private String token;
    private int batchSize;
    private HashMap<Long, Long> pingCorrelation;
    private ArrayList<Long> measurements;

    private HashMap<Long, LatencyMeasurement> latencyCorrelation;
    private ArrayList<Long> q1measurements;

    private double averageLatency;
    private long startNanoTime;
    private AirQualityDataSource datasource;
    private boolean q1Active;
    private boolean q2Active;
    private long benchmarkId;
    private long endNanoTime;

    public BenchmarkState(ArrayBlockingQueue<ToVerify> dbInserter) {
        this.dbInserter = dbInserter;
        this.averageLatency = 0.0;
        this.batchSize = -1;

        this.pingCorrelation = new HashMap<>();
        this.measurements = new ArrayList<>();

        this.latencyCorrelation = new HashMap<>();
        this.q1measurements = new ArrayList<>();

        averageLatency = 0.0;
        startNanoTime = -1;
        endNanoTime = -1;
        datasource = null;

        this.q1Active = false;
        this.q2Active = false;

        this.benchmarkId = -1;
    }

    public void setQ1(boolean contains) {
        this.q1Active = contains;
    }

    public void setQ2(boolean contains) {
        this.q2Active = contains;
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

    public void setBenchmarkId(long random_id) {
        this.benchmarkId = random_id;
    }

    public long getBenchmarkId() {
        return benchmarkId;
    }

    public long getEndNanoTime() {
        return endNanoTime;
    }

    public void setEndNanoTime(long endNanoTime) {
        this.endNanoTime = endNanoTime;
    }


    //Methods for latency measurement
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

    public double calcAverageTransportLatency() {
        if(this.measurements.size() > 0) {
            this.averageLatency = this.measurements.stream().mapToLong(a -> a).average().getAsDouble();
        }

        return this.averageLatency;
    }

    //Starting the benchmark - timestamp
    public void startBenchmark(long startNanoTime) {
        this.startNanoTime = startNanoTime;
    }

    public void setDatasource(AirQualityDataSource newDataSource) {
        this.datasource = newDataSource;
    }

    public AirQualityDataSource getDatasource() {
        return this.datasource;
    }

    public Batch getNextBatch(long benchmarkId) {
        if(this.datasource.hasMoreElements()) {
            Batch batch = this.datasource.nextElement();
            LatencyMeasurement lm = new LatencyMeasurement(benchmarkId, batch.getSeqId(), System.nanoTime());
            this.latencyCorrelation.put(batch.getSeqId(), lm);
            return batch;
        } else {
            try {
                this.datasource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.datasource = null;
            return Batch.newBuilder().setLast(true).build();
        }
    }

    public void resultsQ1(ResultQ1 request, long nanoTime) {
        if(latencyCorrelation.containsKey(request.getPayloadSeqId())) {
            LatencyMeasurement lm = latencyCorrelation.get(request.getPayloadSeqId());
            lm.setQ1Results(nanoTime, request);
            if(isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                latencyCorrelation.remove(request.getPayloadSeqId());
            }
        }
    }

    public void resultsQ2(ResultQ2 request, long nanoTime) {
        if(latencyCorrelation.containsKey(request.getPayloadSeqId())) {
            LatencyMeasurement lm = latencyCorrelation.get(request.getPayloadSeqId());
            lm.setQ2Results(nanoTime, request);
            if(isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                latencyCorrelation.remove(request.getPayloadSeqId());
            }
        }
    }

    private boolean isfinished(LatencyMeasurement lm) {
        if((this.q1Active == lm.hasQ1Results()) && (this.q2Active == lm.hasQ2Results())) {
            return true;
        }
        return false;
    }

    public void endBenchmark(long benchmarkId, long endTime) {
        this.endNanoTime = endTime;
        BenchmarkDuration bd = new BenchmarkDuration(benchmarkId, this.startNanoTime, endTime, this.averageLatency);
        this.dbInserter.add(new ToVerify(bd));
    }

    @Override
    public String toString() {
        return "BenchmarkState{" +
                "token='" + token + '\'' +
                ", benchmarkId=" + benchmarkId +
                ", batchSize=" + batchSize +
                ", averageLatency=" + averageLatency +
                ", startNanoTime=" + startNanoTime +
                ", q1Active=" + q1Active +
                ", q2Active=" + q2Active +
                ", endNanoTime=" + endNanoTime +
                '}';
    }
}
