package org.debs.challenger2.db;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.debs.gc2023.dal.DB;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class NoopQueries implements IQueries{
    @Override
    public boolean checkIfGroupExists(String token) {
        return true;
    }

    @Override
    public ObjectId getGroupIdFromToken(String token) {
        return new ObjectId();
    }

    @Override
    public String getGroupNameFromToken(String token) {
        return "";
    }

    @Override
    public ObjectId createBenchmark(ObjectId groupId, String benchmarkName, String bt) {
        return new ObjectId();
    }

    @Override
    public Document markBenchmarkActive(ObjectId benchmarkId) {
        return new Document("benchmark_id", benchmarkId);
    }

    @Override
    public Document markBenchmarkFinished(ObjectId benchmarkId, Date finishTime) {
        return new Document("benchmark_id", benchmarkId);
    }

    @Override
    public Document getBenchmark(ObjectId benchmarkId) {
        return new Document("benchmark_id", benchmarkId);
    }


    @Override
    public void insertLatency(ObjectId groupId, Integer query, Long latency) {

        return;
    }

    public List<Document> getLatencyAnalysis(ObjectId groupId, Integer query, Date startTime, Date endTime){
        return List.of(new Document());
    }

    @Override
    public void insertBenchmarkResult(ObjectId benchmarkId, Bson results) {
        return;
    }

    @Override
    public void closeDB() {

    }


    @Override
    public DB getDb() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
