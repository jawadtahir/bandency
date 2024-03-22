package org.debs.gc2023.dal;

import com.google.api.SystemParameterOrBuilder;
import org.debs.gc2023.challenger.BenchmarkType;
import org.debs.gc2023.challenger.LatencyMeasurement;
import org.debs.gc2023.dal.dto.BenchmarkResult;
import org.tinylog.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Queries implements IQueries {
    private final DB conn;

    public Queries(DB connectionPool) {
        this.conn = connectionPool;
    }

    @Override
    public DB getDb() {
        return this.conn;
    }

    @Override
    public boolean checkIfGroupExists(String token)
            throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement preparedStatement = this.conn.getConnection().prepareStatement(
                "SELECT count(*) AS rowcount FROM groups where groupapikey = ?")) {
            preparedStatement.setString(1, token);
            try (ResultSet r = preparedStatement.executeQuery()) {
                r.next();
                int count = r.getInt("rowcount");
                return count == 1;
            }
        }
    }

    @Override
    public UUID getGroupIdFromToken(String token)
            throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement preparedStatement = this.conn.getConnection()
                .prepareStatement("SELECT id AS group_id FROM groups where groupapikey = ?")) {
            preparedStatement.setString(1, token);
            try (ResultSet r = preparedStatement.executeQuery()) {
                r.next();
                return r.getObject("group_id", UUID.class);
            }
        }
    }

    @Override
    public String getGroupNameFromToken(String token)
            throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement preparedStatement = this.conn.getConnection()
                .prepareStatement("SELECT groupname FROM groups where groupapikey = ?")) {
            preparedStatement.setString(1, token);
            try (ResultSet r = preparedStatement.executeQuery()) {
                r.next();
                String name = r.getString("groupname");
                return name;
            }
        }
    }

    @Override
    public void insertBenchmarkStarted(long benchmarkId, UUID groupId, String benchmarkName,
            int batchSize, BenchmarkType bt)
            throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement pStmt = this.conn.getConnection()
                .prepareStatement("INSERT INTO benchmarks("
                        + "id, is_active, group_id, \"timestamp\", benchmark_name, benchmark_type, batchsize) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            pStmt.setLong(1, benchmarkId);
            pStmt.setBoolean(2, true);
            pStmt.setObject(3, groupId);
            pStmt.setTimestamp(4, Timestamp.from(Instant.now()));
            pStmt.setString(5, benchmarkName);
            pStmt.setString(6, bt.toString());
            pStmt.setLong(7, batchSize);

            pStmt.execute();
        }
    }

    @Override
    public void insertLatencyMeasurementStats(long benchmarkId, double averageLatency)
            throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement pStmt = this.conn.getConnection()
                .prepareStatement("DELETE FROM latencymeasurement where benchmark_id = ?")) {
            pStmt.setLong(1, benchmarkId);
            pStmt.execute();
        }

        // insert new metrics
        try (PreparedStatement pStmt =
                this.conn.getConnection().prepareStatement("INSERT INTO latencymeasurement("
                        + "benchmark_id, \"timestamp\", avglatency) " + "VALUES (?, ?, ?)")) {

            pStmt.setLong(1, benchmarkId);
            pStmt.setTimestamp(2, Timestamp.from(Instant.now()));
            pStmt.setDouble(3, averageLatency);
            pStmt.execute();
        }
    }

    @Override
    public void insertLatency(LatencyMeasurement lm)
            throws SQLException, ClassNotFoundException, InterruptedException {

        try (PreparedStatement pStmt = this.conn.getConnection()
                .prepareStatement("INSERT INTO querymetrics("
                        + "benchmark_id, batch_id, starttime, q1resulttime, q1latency, q2resulttime, q2latency) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            pStmt.setLong(1, lm.getBenchmarkId());
            pStmt.setLong(2, lm.getBatchId());

            long startTime = lm.getStartTime();
            pStmt.setLong(3, startTime);
            if (lm.hasQ1Results()) {
                long q1resultTime = lm.getQ1ResultTime();
                long q1Latency = q1resultTime - startTime;

                pStmt.setLong(4, q1resultTime);
                pStmt.setLong(5, q1Latency);
            } else {
                pStmt.setLong(4, java.sql.Types.NULL);
                pStmt.setLong(5, java.sql.Types.NULL);
            }

            if (lm.hasQ2Results()) {
                long q2resultTime = lm.getQ2ResultTime();
                long q2Latency = q2resultTime - startTime;

                pStmt.setLong(6, q2resultTime);
                pStmt.setLong(7, q2Latency);
            } else {
                pStmt.setNull(6, java.sql.Types.NULL);
                pStmt.setLong(7, java.sql.Types.NULL);
            }

            pStmt.execute();
        }
    }

    @Override
    public void insertBenchmarkResult(BenchmarkResult br, String s)
            throws SQLException, ClassNotFoundException, InterruptedException {

        try (PreparedStatement pStmt = this.conn.getConnection()
                .prepareStatement("INSERT INTO public.benchmarkresults("
                        + "id, duration_sec, q1_count, q1_throughput, q1_90percentile, q2_count, q2_throughput, q2_90percentile, summary) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            pStmt.setLong(1, br.getBenchmarkId());
            pStmt.setDouble(2, br.getSeconds());

            pStmt.setLong(3, br.getQ1_count());
            pStmt.setDouble(4, br.getQ1Throughput());
            pStmt.setDouble(5, br.getQ1_90Percentile());

            pStmt.setLong(6, br.getQ2_count());
            pStmt.setDouble(7, br.getQ2Throughput());
            pStmt.setDouble(8, br.getQ2_90Percentile());

            pStmt.setString(9, s);

            pStmt.execute();
        }
    }

    @Override
    public void insertLatency(LatencyMeasurement lm, boolean s)
            throws SQLException, ClassNotFoundException, InterruptedException {
        Logger.info("inserting into querymetrics...");
        try (PreparedStatement pStmt = this.conn.getConnection()
                .prepareStatement("INSERT INTO querymetrics1("
                        + "benchmark_id, batch_id, starttime, q1resulttime, q1latency,q1failureresulttime,"
                        + "q1failurelatency,q1postfailureresulttime,q1postfailurelatency,"
                        + " q2resulttime, q2latency,q2failureresulttime,q2failurelatency,q2postfailureresulttime,"
                        + "q2postfailurelatency) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            pStmt.setLong(1, lm.getBenchmarkId());
            pStmt.setLong(2, lm.getBatchId());

            long startTime = lm.getStartTime();
            pStmt.setLong(3, startTime);
            if (lm.hasQ1Results()) {
                if (lm.hasQ1FailureResults()) {
                    long q1failureresultTime = lm.getQ1FailureResultTime();
                    long q1FailureLatency = q1failureresultTime - startTime;
                    pStmt.setLong(4, java.sql.Types.NULL);
                    pStmt.setLong(5, java.sql.Types.NULL);
                    pStmt.setLong(6, q1failureresultTime);
                    pStmt.setLong(7, q1FailureLatency);
                    pStmt.setLong(8, java.sql.Types.NULL);
                    pStmt.setLong(9, java.sql.Types.NULL);
                } else if (lm.hasQ1PostFailureResults()) {
                    long q1PostresultTime = lm.getQ1PostFailureResultTime();
                    long q1PostFailureLatency = q1PostresultTime - startTime;
                    pStmt.setLong(4, java.sql.Types.NULL);
                    pStmt.setLong(5, java.sql.Types.NULL);
                    pStmt.setLong(6, java.sql.Types.NULL);
                    pStmt.setLong(7, java.sql.Types.NULL);
                    pStmt.setLong(8, q1PostresultTime);
                    pStmt.setLong(9, q1PostFailureLatency);
                } else {
                    long q1resultTime = lm.getQ1ResultTime();
                    long q1PreFailureLatency = q1resultTime - startTime;
                    pStmt.setLong(4, q1resultTime);
                    pStmt.setLong(5, q1PreFailureLatency);
                    pStmt.setLong(6, java.sql.Types.NULL);
                    pStmt.setLong(7, java.sql.Types.NULL);
                    pStmt.setLong(8, java.sql.Types.NULL);
                    pStmt.setLong(9, java.sql.Types.NULL);
                }
            } else {
                pStmt.setLong(4, java.sql.Types.NULL);
                pStmt.setLong(5, java.sql.Types.NULL);
                pStmt.setLong(6, java.sql.Types.NULL);
                pStmt.setLong(7, java.sql.Types.NULL);
                pStmt.setLong(8, java.sql.Types.NULL);
                pStmt.setLong(9, java.sql.Types.NULL);
            }

            if (lm.hasQ2Results()) {
                if (lm.hasQ2FailureResults()) {
                    long q2failureresultTime = lm.getQ2FailureResultTime();
                    long q2FailureLatency = q2failureresultTime - startTime;
                    pStmt.setLong(10, java.sql.Types.NULL);
                    pStmt.setLong(11, java.sql.Types.NULL);
                    pStmt.setLong(12, q2failureresultTime);
                    pStmt.setLong(13, q2FailureLatency);
                    pStmt.setLong(14, java.sql.Types.NULL);
                    pStmt.setLong(15, java.sql.Types.NULL);
                } else if (lm.hasQ2PostFailureResults()) {
                    long q2PostresultTime = lm.getQ2PostFailureResultTime();
                    long q2PostFailureLatency = q2PostresultTime - startTime;
                    pStmt.setLong(10, java.sql.Types.NULL);
                    pStmt.setLong(11, java.sql.Types.NULL);
                    pStmt.setLong(12, java.sql.Types.NULL);
                    pStmt.setLong(13, java.sql.Types.NULL);
                    pStmt.setLong(14, q2PostresultTime);
                    pStmt.setLong(15, q2PostFailureLatency);
                } else {
                    long q2resultTime = lm.getQ2ResultTime();
                    long q2PreFailureLatency = q2resultTime - startTime;
                    pStmt.setLong(10, q2resultTime);
                    pStmt.setLong(11, q2PreFailureLatency);
                    pStmt.setLong(12, java.sql.Types.NULL);
                    pStmt.setLong(13, java.sql.Types.NULL);
                    pStmt.setLong(14, java.sql.Types.NULL);
                    pStmt.setLong(15, java.sql.Types.NULL);
                }
            } else {
                pStmt.setLong(10, java.sql.Types.NULL);
                pStmt.setLong(11, java.sql.Types.NULL);
                pStmt.setLong(12, java.sql.Types.NULL);
                pStmt.setLong(13, java.sql.Types.NULL);
                pStmt.setLong(14, java.sql.Types.NULL);
                pStmt.setLong(15, java.sql.Types.NULL);
            }

            pStmt.execute();
        }
    }

    @Override
    public void insertBenchmarkResult(BenchmarkResult br, String s, boolean v)
            throws SQLException, ClassNotFoundException, InterruptedException {
        Logger.info("inserting into benchmarkresults..");
        try (PreparedStatement pStmt = this.conn.getConnection()
                .prepareStatement("INSERT INTO benchmarkresults1("
                        + "id, duration_sec, q1_count,q1_failurecount,q1_postfailurecount, q1_throughput,"
                        + "q1_failurethroughput,q1_postfailurethroughput, q1_90percentile,q1_failure90percentile,q1_postfailure90percentile,"
                        + " q2_count,q2_failurecount,q2_postfailurecount, q2_throughput,q2_failurethroughput,q2_postfailurethroughtput, q2_90percentile,"
                        + "q2failure90percentile,q2postfailure90percentile, summary) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            pStmt.setLong(1, br.getBenchmarkId());
            pStmt.setDouble(2, br.getSeconds());
            pStmt.setLong(3, br.getQ1_count());
            pStmt.setLong(4, br.getQ1_Failure_count());
            pStmt.setLong(5, br.getQ1_Post_Failure_count());
            pStmt.setDouble(6, br.getQ1Throughput());
            pStmt.setDouble(7, br.getQ1FailureThroughput());
            pStmt.setDouble(8, br.getQ1PostFailureThroughput());
            pStmt.setDouble(9, br.getQ1_90Percentile());
            pStmt.setDouble(10, br.getQ1_Failure90Percentile());
            pStmt.setDouble(11, br.getQ1_PostFailure90Percentile());
            pStmt.setLong(12, br.getQ2_count());
            pStmt.setLong(13, br.getQ2_Failure_count());
            pStmt.setLong(14, br.getQ2_Post_Failure_count());
            pStmt.setDouble(15, br.getQ2Throughput());
            pStmt.setDouble(16, br.getQ2FailureThroughput());
            pStmt.setDouble(17, br.getQ2PostFailureThroughput());
            pStmt.setDouble(18, br.getQ2_90Percentile());
            pStmt.setDouble(19, br.getQ2_Failure90Percentile());
            pStmt.setDouble(20, br.getQ2_PostFailure90Percentile());

            pStmt.setString(21, s);

            pStmt.execute();
        }
    }


    @Override
    public List<String> getVirtualMachineInfo(String token)
            throws SQLException, ClassNotFoundException, InterruptedException {
        Logger.info("Token: " + token);

        List<String> internalAdresses = new ArrayList<>();

        try (PreparedStatement preparedStatement = this.conn.getConnection()
                .prepareStatement("SELECT virtualmachines.forwardingadrs " + "FROM groups "
                        + "JOIN virtualmachines ON groups.id = virtualmachines.group_id "
                        + "WHERE groups.groupapikey = ?")) {
            preparedStatement.setString(1, token);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String forwardingadrs = resultSet.getString("forwardingadrs");
                    internalAdresses.add(forwardingadrs);
                }
            }
        }

        return internalAdresses;
    }

}
