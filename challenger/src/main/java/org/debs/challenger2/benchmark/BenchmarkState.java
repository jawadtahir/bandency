package org.debs.challenger2.benchmark;

import com.google.protobuf.InvalidProtocolBufferException;
import org.HdrHistogram.Histogram;
import org.bson.types.ObjectId;
import org.debs.challenger2.dataset.BatchIterator;
import org.debs.challenger2.rest.dao.Batch;
import org.debs.challenger2.rest.dao.ResultQ1;
import org.debs.challenger2.rest.dao.ResultQ2;
import org.rocksdb.RocksDBException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class BenchmarkState {
    private final ArrayBlockingQueue<ToVerify> dbInserter;
    private String token;
    private int batchSize;
    private boolean isStarted;
    private HashMap<Long, Long> pingCorrelation;
    private ArrayList<Long> measurements;

    private HashMap<Long, LatencyMeasurement> latencyCorrelation;
    private ArrayList<Long> q1measurements;

    private Histogram q1Histogram;
    private Histogram q2Histogram;

    private double averageLatency;
    private long startNanoTime;
    private BatchIterator datasource;
    private boolean q1Active;
    private boolean q2Active;
    private ObjectId benchmarkId;
    private long endNanoTime;
    private BenchmarkType benchmarkType;
    private String benchmarkName;

    public BenchmarkState(ArrayBlockingQueue<ToVerify> dbInserter) {
        this.dbInserter = dbInserter;
        this.averageLatency = 0.0;
        this.batchSize = -1;
        this.isStarted = false;

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

        this.q1Histogram = new Histogram( 3);
        this.q2Histogram = new Histogram( 3);

        this.benchmarkId = null;

        this.benchmarkType = BenchmarkType.Test;
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

    public void setIsStarted(boolean istarted) {
        this.isStarted = istarted;
    }

    public boolean getIsStarted() {
        return this.isStarted;
    }

    public String getToken() {
        return token;
    }

    public void setBenchmarkId(ObjectId random_id) {
        this.benchmarkId = random_id;
    }

    public ObjectId getBenchmarkId() {
        return benchmarkId;
    }

    public long getEndNanoTime() {
        return endNanoTime;
    }

    public void setEndNanoTime(long endNanoTime) {
        this.endNanoTime = endNanoTime;
    }

    public BenchmarkType getBenchmarkType() {
        return benchmarkType;
    }

    public void setBenchmarkType(BenchmarkType benchmarkType) {
        this.benchmarkType = benchmarkType;
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

    public void setDatasource(BatchIterator newDataSource) {
        this.datasource = newDataSource;
    }

    public BatchIterator getDatasource() {
        return this.datasource;
    }

    public Batch getNextBatch(ObjectId benchmarkId) throws RocksDBException, InterruptedException, InvalidProtocolBufferException {

        if(this.datasource == null) { //when participants ignore the last flag
            Batch batchJSON = new Batch();
            batchJSON.setLast(true);
            return batchJSON;
        }
        if(this.datasource.hasMoreElements()) {
            // Logger.info("Has more elements: " + benchmarkId);
            Batch batch = this.datasource.nextElement();
            LatencyMeasurement lm = new LatencyMeasurement(benchmarkId, batch.getSeqId(), System.nanoTime());
            this.latencyCorrelation.put(batch.getSeqId(), lm);
            return batch;
        } else {
            //Logger.info("No more elements" + benchmarkId);
            this.datasource = null;
            Batch batchJSON = new Batch();
            batchJSON.setLast(true);
            return batchJSON;
        }
    }

    public void resultsQ1(ResultQ1 request, long nanoTime) {
        if (latencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = latencyCorrelation.get(request.getBatchSeqId());
            lm.setQ1Results(nanoTime, request);
            q1Histogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                latencyCorrelation.remove(request.getBatchSeqId());
            }
        }
    }

    public void resultsQ2(ResultQ2 request, long nanoTime) {
        if (latencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = latencyCorrelation.get(request.getBatchSeqId());
            lm.setQ2Results(nanoTime, request);
            q2Histogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                latencyCorrelation.remove(request.getBatchSeqId());
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

        BenchmarkDuration bd = new BenchmarkDuration(
                benchmarkId,
                this.startNanoTime,
                endTime,
                this.averageLatency,
                q1Histogram,
                q2Histogram,
                this.q1Active,
                this.q2Active);
        this.dbInserter.add(new ToVerify(bd));
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }
}
