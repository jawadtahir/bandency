package org.debs.challenger2.benchmark;

import org.HdrHistogram.Histogram;
import org.bson.types.ObjectId;
import org.debs.challenger2.dataset.IDataStore;
import org.debs.challenger2.pending.BenchmarkDuration;
import org.debs.challenger2.pending.IPendingTask;
import org.debs.challenger2.pending.LatencyMeasurement;
import org.debs.challenger2.rest.dao.Batch;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class BenchmarkState {
    private final ArrayBlockingQueue<IPendingTask> pending;
    private String token;

    private ObjectId benchmarkId;
    private ObjectId groupId;
    private boolean isStarted;

    private ConcurrentHashMap<Long, LatencyMeasurement> latencyCorrelation;

    private ConcurrentHashMap<Integer, Histogram> histograms;

    private long startNanoTime;

    private IDataStore dataStore;
    private long pointer;

    private long endNanoTime;
    private String benchmarkName;

    public BenchmarkState(ArrayBlockingQueue<IPendingTask> pending) {
        this.pending = pending;
        this.isStarted = false;

        this.latencyCorrelation = new ConcurrentHashMap<>();

        startNanoTime = -1;
        endNanoTime = -1;
        dataStore = null;
        pointer = 0L;

        this.histograms = new ConcurrentHashMap<>();


        this.benchmarkId = null;

    }

    public Batch getNextBatch(String benchmarkId){

        if(++pointer <= dataStore.batchCount()) {
            Batch batch = dataStore.getBatch(pointer);
            ObjectId benchmarkOid = new ObjectId(benchmarkId);
            LatencyMeasurement lm = new LatencyMeasurement(groupId, benchmarkOid, batch.getSeqId(), System.nanoTime());
            this.latencyCorrelation.put(batch.getSeqId(), lm);
            return batch;
        } else {

            Batch batchJSON = new Batch();
            batchJSON.setLast(true);
            return batchJSON;
        }
    }

    public void markResult(Long batchId, Long endNanoTime, Integer query){

        if (latencyCorrelation.containsKey(batchId)){
            LatencyMeasurement lm = latencyCorrelation.get(batchId);
            lm.markEnd(query, endNanoTime);
//            if (!histograms.containsKey(query)){
//                histograms.put(query, new Histogram(3));
//            }
//            histograms.get(query).recordValue(endNanoTime - lm.getStartTime());
            this.pending.add(lm);
        }
    }



    public void endBenchmark(Date endTime) {

        BenchmarkDuration bd = new BenchmarkDuration(
                groupId,
                benchmarkId,
                endTime);
        this.pending.add(bd);
    }

    public ArrayBlockingQueue<IPendingTask> getPending() {
        return pending;
    }

    public IDataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(IDataStore dataStore) {
        this.dataStore = dataStore;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ObjectId getBenchmarkId() {
        return benchmarkId;
    }

    public void setBenchmarkId(ObjectId benchmarkId) {
        this.benchmarkId = benchmarkId;
    }

    public ObjectId getGroupId() {
        return groupId;
    }

    public void setGroupId(ObjectId groupId) {
        this.groupId = groupId;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public ConcurrentHashMap<Long, LatencyMeasurement> getLatencyCorrelation() {
        return latencyCorrelation;
    }

    public void setLatencyCorrelation(ConcurrentHashMap<Long, LatencyMeasurement> latencyCorrelation) {
        this.latencyCorrelation = latencyCorrelation;
    }

    public ConcurrentHashMap<Integer, Histogram> getHistograms() {
        return histograms;
    }

    public void setHistograms(ConcurrentHashMap<Integer, Histogram> histograms) {
        this.histograms = histograms;
    }

    public long getStartNanoTime() {
        return startNanoTime;
    }

    public void setStartNanoTime(long startNanoTime) {
        this.startNanoTime = startNanoTime;
    }

    public long getEndNanoTime() {
        return endNanoTime;
    }

    public void setEndNanoTime(long endNanoTime) {
        this.endNanoTime = endNanoTime;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }
}
