package de.tum.i13.dal;

import de.tum.i13.challenger.BenchmarkType;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;

public class Queries {
    private final Connection conn;

    public Queries(Connection conn) {

        this.conn = conn;
    }

    public boolean checkIfGroupExists(String token) throws SQLException {
        try(PreparedStatement preparedStatement = this.conn.prepareStatement("SELECT count(*) AS rowcount FROM groups where groupapikey = ?")) {
            preparedStatement.setString(1, token);
            try(ResultSet r = preparedStatement.executeQuery()) {
                r.next();
                int count = r.getInt("rowcount");
                return count == 1;
            }
        }
    }

    public UUID getGroupIdFromToken(String token) throws SQLException {
        try(PreparedStatement preparedStatement = this.conn
                .prepareStatement("SELECT id AS group_id FROM groups where groupapikey = ?")) {
            preparedStatement.setString(1, token);
            try(ResultSet r = preparedStatement.executeQuery()) {
                r.next();
                return r.getObject("group_id", UUID.class);
            }
        }
    }

    public void insertBenchmarkStarted(long benchmarkId, UUID groupId, String benchmarkName, int batchSize, BenchmarkType bt) throws SQLException {
        try(PreparedStatement pStmt = this.conn
                .prepareStatement("INSERT INTO public.benchmark(" +
                        "id, group_id, \"timestamp\", benchmark_name, benchmark_type, batchsize) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {

            pStmt.setLong(1, benchmarkId);
            pStmt.setObject(2, groupId);
            pStmt.setTimestamp(3, Timestamp.from(Instant.now()));
            pStmt.setString(4, benchmarkName);
            pStmt.setString(5, bt.toString());
            pStmt.setLong(6, batchSize);

            pStmt.execute();
        }
    }

    public void insertLatencyMeasurementStats(long benchmarkId, double averageLatency) {

    }
}
