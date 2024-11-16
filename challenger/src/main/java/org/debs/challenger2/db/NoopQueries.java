package org.debs.challenger2.db;

import org.bson.types.ObjectId;
import org.debs.challenger2.benchmark.LatencyMeasurement;
import org.debs.gc2023.dal.DB;
import org.debs.gc2023.dal.dto.BenchmarkResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public ObjectId insertBenchmarkStarted(ObjectId groupId, String benchmarkName,
                                       int batchSize, String bt) {

        return new ObjectId();
    }

    @Override
    public void insertLatencyMeasurementStats(ObjectId benchmarkId, double averageLatency) {
        return;
    }

    @Override
    public void insertLatency(LatencyMeasurement lm) {

        return;
    }

    @Override
    public void insertBenchmarkResult(BenchmarkResult br, String s) {
        return;
    }

    @Override
    public List<String> getVirtualMachineInfo(String token) {
        List<String> l = new ArrayList<>();
        return l;
    }

    @Override
    public DB getDb() {
        return null;
    }

    @Override
    public void insertLatency(LatencyMeasurement lm, boolean s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertLatency'");
    }

    @Override
    public void insertBenchmarkResult(BenchmarkResult br, String s, boolean v) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertBenchmarkResult'");
    }
}
