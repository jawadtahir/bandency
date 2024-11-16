package org.debs.challenger2.db;

import org.bson.types.ObjectId;
import org.debs.challenger2.benchmark.LatencyMeasurement;
import org.debs.gc2023.dal.DB;
import org.debs.gc2023.dal.dto.BenchmarkResult;

import java.util.List;
import java.util.UUID;

public interface IQueries {
    DB getDb();

    boolean checkIfGroupExists(String token);

    ObjectId getGroupIdFromToken(String token);

    String getGroupNameFromToken(String token);

    ObjectId insertBenchmarkStarted(ObjectId groupId, String benchmarkName, int batchSize,
                                String bt);

    void insertLatencyMeasurementStats(ObjectId benchmarkId, double averageLatency);

    void insertLatency(LatencyMeasurement lm);

    void insertLatency(LatencyMeasurement lm, boolean s);

    void insertBenchmarkResult(BenchmarkResult br, String s);

    void insertBenchmarkResult(BenchmarkResult br, String s, boolean v);

    List<String> getVirtualMachineInfo(String token);
}
