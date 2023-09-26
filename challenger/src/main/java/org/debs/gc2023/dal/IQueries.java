package org.debs.gc2023.dal;

import java.sql.SQLException;
import java.util.UUID;

import org.debs.gc2023.challenger.BenchmarkType;
import org.debs.gc2023.challenger.LatencyMeasurement;
import org.debs.gc2023.dal.dto.BenchmarkResult;

public interface IQueries {
    DB getDb();

    boolean checkIfGroupExists(String token) throws SQLException, ClassNotFoundException, InterruptedException;

    UUID getGroupIdFromToken(String token) throws SQLException, ClassNotFoundException, InterruptedException;

    void insertBenchmarkStarted(long benchmarkId, UUID groupId, String benchmarkName, int batchSize, BenchmarkType bt)
            throws SQLException, ClassNotFoundException, InterruptedException;

    void insertLatencyMeasurementStats(long benchmarkId, double averageLatency)
            throws SQLException, ClassNotFoundException, InterruptedException;

    void insertLatency(LatencyMeasurement lm) throws SQLException, ClassNotFoundException, InterruptedException;

    void insertBenchmarkResult(BenchmarkResult br, String s)
            throws SQLException, ClassNotFoundException, InterruptedException;

}