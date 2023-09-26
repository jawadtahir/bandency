package org.debs.gc2023.dal;

import java.sql.SQLException;
import java.util.UUID;

import org.debs.gc2023.challenger.BenchmarkType;
import org.debs.gc2023.challenger.LatencyMeasurement;
import org.debs.gc2023.dal.dto.BenchmarkResult;

public class NoopQueries implements IQueries {

    @Override
    public boolean checkIfGroupExists(String token) throws SQLException, ClassNotFoundException, InterruptedException {
        return true;
    }

    @Override
    public UUID getGroupIdFromToken(String token) throws SQLException, ClassNotFoundException, InterruptedException {
        return UUID.randomUUID();
    }

    @Override
    public void insertBenchmarkStarted(long benchmarkId, UUID groupId, String benchmarkName, int batchSize,
            BenchmarkType bt) throws SQLException, ClassNotFoundException, InterruptedException {
        
        return;
    }

    @Override
    public void insertLatencyMeasurementStats(long benchmarkId, double averageLatency)
            throws SQLException, ClassNotFoundException, InterruptedException {
        
        return;
    }

    @Override
    public void insertLatency(LatencyMeasurement lm) throws SQLException, ClassNotFoundException, InterruptedException {
        
        return;
    }

    @Override
    public void insertBenchmarkResult(BenchmarkResult br, String s)
            throws SQLException, ClassNotFoundException, InterruptedException {
        
        return;
    }

    @Override
    public DB getDb() {
        return null;
    }
    
}
