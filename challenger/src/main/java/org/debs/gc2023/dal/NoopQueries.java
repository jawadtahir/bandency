package org.debs.gc2023.dal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.debs.gc2023.challenger.BenchmarkType;
import org.debs.gc2023.challenger.LatencyMeasurement;
import org.debs.gc2023.dal.dto.BenchmarkResult;

public class NoopQueries implements IQueries {

    @Override
    public boolean checkIfGroupExists(String token)
            throws SQLException, ClassNotFoundException, InterruptedException {
        return true;
    }

    @Override
    public UUID getGroupIdFromToken(String token)
            throws SQLException, ClassNotFoundException, InterruptedException {
        return UUID.randomUUID();
    }

    @Override
    public String getGroupNameFromToken(String token)
            throws SQLException, ClassNotFoundException, InterruptedException {
        return "";
    }

    @Override
    public void insertBenchmarkStarted(long benchmarkId, UUID groupId, String benchmarkName,
            int batchSize, BenchmarkType bt)
            throws SQLException, ClassNotFoundException, InterruptedException {

        return;
    }

    @Override
    public void insertLatencyMeasurementStats(long benchmarkId, double averageLatency)
            throws SQLException, ClassNotFoundException, InterruptedException {
        return;
    }

    @Override
    public void insertLatency(LatencyMeasurement lm)
            throws SQLException, ClassNotFoundException, InterruptedException {

        return;
    }

    @Override
    public void insertBenchmarkResult(BenchmarkResult br, String s)
            throws SQLException, ClassNotFoundException, InterruptedException {
        return;
    }

    @Override
    public List<String> getVirtualMachineInfo(String token)
            throws SQLException, ClassNotFoundException, InterruptedException {
        List<String> l = new ArrayList<>();
        return l;
    }

    @Override
    public DB getDb() {
        return null;
    }

    @Override
    public void insertLatency(LatencyMeasurement lm, boolean s)
            throws SQLException, ClassNotFoundException, InterruptedException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertLatency'");
    }

    @Override
    public void insertBenchmarkResult(BenchmarkResult br, String s, boolean v)
            throws SQLException, ClassNotFoundException, InterruptedException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertBenchmarkResult'");
    }

}
