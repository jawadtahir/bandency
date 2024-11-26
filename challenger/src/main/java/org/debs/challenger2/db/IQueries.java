package org.debs.challenger2.db;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.debs.gc2023.dal.DB;

import java.io.Closeable;
import java.util.Date;
import java.util.List;

public interface IQueries extends Closeable {
    DB getDb();

    boolean checkIfGroupExists(String token);

    ObjectId getGroupIdFromToken(String token);

    String getGroupNameFromToken(String token);

    public ObjectId createBenchmark(ObjectId groupId, String benchmarkName, String bt);

    public Document markBenchmarkActive(ObjectId benchmarkId);

    public Document markBenchmarkFinished(ObjectId benchmarkId, Date finishTime);

    public Document getBenchmark (ObjectId benchmarkId);


    void insertLatency(ObjectId groupId, Integer query, Long latency);
    public List<Document> getLatencyAnalysis(ObjectId groupId, Integer query, Date startTime, Date endTime);


    void insertBenchmarkResult(ObjectId benchmarkId, Bson results);

    void closeDB();


}
