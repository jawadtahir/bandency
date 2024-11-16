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
import java.util.concurrent.ConcurrentHashMap;

public class BenchmarkState {
    private final ArrayBlockingQueue<ToVerify> dbInserter;
    private String token;

    private ObjectId benchmarkId;
    private int batchSize;
    private boolean isStarted;

    private ConcurrentHashMap<Long, LatencyMeasurement> latencyCorrelation;

    private ConcurrentHashMap<Integer, Histogram> histograms;

    private long startNanoTime;
    private BatchIterator datasource;

    private long endNanoTime;
    private BenchmarkType benchmarkType;
    private String benchmarkName;

    public BenchmarkState(ArrayBlockingQueue<ToVerify> dbInserter) {
        this.dbInserter = dbInserter;
        this.batchSize = -1;
        this.isStarted = false;

        this.latencyCorrelation = new ConcurrentHashMap<>();

        startNanoTime = -1;
        endNanoTime = -1;
        datasource = null;

        this.histograms = new ConcurrentHashMap<>();


//        this.q1Histogram = new Histogram( 3);
//        this.q2Histogram = new Histogram( 3);

        this.benchmarkId = null;

        this.benchmarkType = BenchmarkType.Test;
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

    public boolean markResult(Long batchId, Long endNanoTime, Integer query){
        boolean retVal = false;

        if (latencyCorrelation.containsKey(batchId)){
            LatencyMeasurement lm = latencyCorrelation.get(batchId);
            lm.markEnd(query, endNanoTime);
            retVal = true;
            if (!histograms.containsKey(query)){
                histograms.put(query, new Histogram(3));
            }
            histograms.get(query).recordValue(endNanoTime - lm.getStartTime());
        } else {
            retVal = false;
        }
        return retVal;
    }



    public void endBenchmark(String benchmarkId, long endTime) {
        this.endNanoTime = endTime;

        BenchmarkDuration bd = new BenchmarkDuration(
                benchmarkId,
                this.startNanoTime,
                endTime,
                histograms);
        this.dbInserter.add(new ToVerify(bd));
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }
}
