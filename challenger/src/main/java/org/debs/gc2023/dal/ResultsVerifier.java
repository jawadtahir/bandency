package org.debs.gc2023.dal;

import com.google.gson.Gson;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import org.debs.gc2023.challenger.LatencyMeasurement;
import org.debs.gc2023.dal.dto.BenchmarkResult;
import org.debs.gc2023.dal.dto.PercentileResult;
import org.tinylog.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ResultsVerifier implements Runnable {
        private final ArrayBlockingQueue<ToVerify> verificationQueue;
        private final IQueries q;
        private AtomicReference<Boolean> shutdown;
        private AtomicReference<Boolean> shuttingDown;

        public ResultsVerifier(ArrayBlockingQueue<ToVerify> verificationQueue, IQueries q) {
                this.verificationQueue = verificationQueue;
                this.q = q;
                this.shuttingDown = new AtomicReference<Boolean>(false);
                this.shutdown = new AtomicReference<Boolean>(true);
        }

        static final Counter verifyMeasurementCounter = Counter.build()
                        .name("verifyMeasurementCounter")
                        .help("calls to verifyMeasurementCounter methods").register();

        static final Counter durationMeasurementCounter = Counter.build()
                        .name("durationMeasurementCounter")
                        .help("calls to durationMeasurementCounter methods").register();

        static final Histogram resultVerificationQueue = Histogram.build().name("verificationQueue")
                        .help("verificationQueue of Resultsverifier")
                        .linearBuckets(0.0, 1_000.0, 21).create().register();

        static final Counter resultVerificationErrors = Counter.build().name("verificationErrors")
                        .help("counter of errors which currently are unhandled").register();

        @Override
        public void run() {
                this.shuttingDown.set(false);
                this.shutdown.set(false);

                while (!shuttingDown.get() || verificationQueue.size() > 0) {
                        try {
                                // TODO; check if we need to change something in this part
                                ToVerify poll = verificationQueue.poll(100, TimeUnit.MILLISECONDS);
                                resultVerificationQueue.observe(verificationQueue.size());
                                if (poll != null) {
                                        if (poll.getType() == VerificationType.Measurement) {
                                                LatencyMeasurement lm =
                                                                poll.getLatencyMeasurement();
                                                try {
                                                        if (lm.hasFailureResults()) {
                                                                q.insertLatency(lm, true);
                                                        } else {
                                                                q.insertLatency(lm);
                                                        }
                                                } catch (SQLException
                                                                | ClassNotFoundException throwables) {
                                                        // We have to handle that gracefully
                                                        throwables.printStackTrace();

                                                        resultVerificationErrors.inc();
                                                }

                                                verifyMeasurementCounter.inc();
                                        } else if (poll.getType() == VerificationType.Duration) {
                                                BenchmarkDuration benchmarkDuration =
                                                                poll.getBenchmarkDuration();
                                                benchmarkDuration.getStartTime();

                                                double[] percentiles = new double[] {50.0, 75.0,
                                                                87.5, 90, 95, 97.5, 99, 99.9};

                                                var pl = new ArrayList<PercentileResult>();

                                                if (benchmarkDuration.failureActive) {

                                                        for (double percentile : percentiles) {
                                                                var p = new PercentileResult(
                                                                                percentile);
                                                                if (benchmarkDuration
                                                                                .isQ1Active()) {
                                                                        long preFailureValueAtPercentile =
                                                                                        benchmarkDuration
                                                                                                        .getQ1Histogram()
                                                                                                        .getValueAtPercentile(
                                                                                                                        percentile);
                                                                        long duringFailureValueAtPercentile =
                                                                                        benchmarkDuration
                                                                                                        .getQ1FailureHistogram()
                                                                                                        .getValueAtPercentile(
                                                                                                                        percentile);
                                                                        long postFailureValueAtPercentile =
                                                                                        benchmarkDuration
                                                                                                        .getQ1PostFailureHistogram()
                                                                                                        .getValueAtPercentile(
                                                                                                                        percentile);
                                                                        p.setQ1Latency(preFailureValueAtPercentile);
                                                                        p.setQ1FailureLatency(
                                                                                        duringFailureValueAtPercentile);
                                                                        p.setQ1PostFailureLatency(
                                                                                        postFailureValueAtPercentile);
                                                                }
                                                                if (benchmarkDuration
                                                                                .isQ2Active()) {
                                                                        long preFailureValueAtPercentile =
                                                                                        benchmarkDuration
                                                                                                        .getQ2Histogram()
                                                                                                        .getValueAtPercentile(
                                                                                                                        percentile);
                                                                        long duringFailureValueAtPercentile =
                                                                                        benchmarkDuration
                                                                                                        .getQ2FailureHistogram()
                                                                                                        .getValueAtPercentile(
                                                                                                                        percentile);
                                                                        long postFailureValueAtPercentile =
                                                                                        benchmarkDuration
                                                                                                        .getQ2PostFailureHistogram()
                                                                                                        .getValueAtPercentile(
                                                                                                                        percentile);
                                                                        p.setQ2Latency(preFailureValueAtPercentile);
                                                                        p.setQ2FailureLatency(
                                                                                        duringFailureValueAtPercentile);
                                                                        p.setQ2PostFailureLatency(
                                                                                        postFailureValueAtPercentile);
                                                                }
                                                                pl.add(p);
                                                        }

                                                        double q1_90Percentile = benchmarkDuration
                                                                        .isQ1Active() ? benchmarkDuration.getQ1Histogram().getValueAtPercentile(90) / 1e6 : -1.0;
                                                        double q1Failure90Percentile =
                                                                        benchmarkDuration
                                                                                        .isQ1Active() ? benchmarkDuration.getQ1FailureHistogram().getValueAtPercentile(90) / 1e6 : -1.0;
                                                        double q1PostFailure90Percentile =
                                                                        benchmarkDuration
                                                                                        .isQ1Active() ? benchmarkDuration.getQ1PostFailureHistogram().getValueAtPercentile(90) / 1e6 : -1.0;
                                                        double q2_90Percentile = benchmarkDuration
                                                                        .isQ2Active() ? benchmarkDuration.getQ2Histogram().getValueAtPercentile(90) / 1e6 : -1.0;
                                                        double q2Failure90Percentile =
                                                                        benchmarkDuration
                                                                                        .isQ1Active() ? benchmarkDuration.getQ2FailureHistogram().getValueAtPercentile(90) / 1e6 : -1.0;
                                                        double q2PostFailure90Percentile =
                                                                        benchmarkDuration
                                                                                        .isQ1Active() ? benchmarkDuration.getQ2PostFailureHistogram().getValueAtPercentile(90) / 1e6 : -1.0;


                                                        // TODO; whats the point of these calls,
                                                        // comment em out.
                                                        benchmarkDuration.getStartTime();
                                                        benchmarkDuration.getEndTime();
                                                        benchmarkDuration.getQ1Histogram()
                                                                        .getTotalCount();
                                                        benchmarkDuration
                                                                        .getQ1PostFailureHistogram()
                                                                        .getTotalCount();
                                                        benchmarkDuration.getQ1FailureHistogram()
                                                                        .getTotalCount();
                                                        benchmarkDuration.getQ2Histogram()
                                                                        .getTotalCount();
                                                        benchmarkDuration
                                                                        .getQ2PostFailureHistogram()
                                                                        .getTotalCount();
                                                        benchmarkDuration.getQ2FailureHistogram()
                                                                        .getTotalCount();

                                                        double preFailureSeconds =
                                                                        (benchmarkDuration
                                                                                        .getEndPrefailureNanoTime()
                                                                                        - benchmarkDuration
                                                                                                        .getStartTime())
                                                                                        / 1e9;
                                                        double failureSeconds = (benchmarkDuration
                                                                        .getEndFailureTime()
                                                                        - benchmarkDuration
                                                                                        .getStartFailureTime())
                                                                        / 1e9;
                                                        double postFailureSeconds =
                                                                        (benchmarkDuration
                                                                                        .getEndTime()
                                                                                        - benchmarkDuration
                                                                                                        .getStartPostfailureTime())
                                                                                        / 1e9;
                                                        double q1Throughput = benchmarkDuration
                                                                        .getQ1Histogram()
                                                                        .getTotalCount()
                                                                        / preFailureSeconds;
                                                        double q1FailureThroughput =
                                                                        benchmarkDuration
                                                                                        .getQ1FailureHistogram()
                                                                                        .getTotalCount()
                                                                                        / failureSeconds;
                                                        double q1PostFailureThroughput =
                                                                        benchmarkDuration
                                                                                        .getQ1PostFailureHistogram()
                                                                                        .getTotalCount()
                                                                                        / postFailureSeconds;
                                                        double q2Throughput = benchmarkDuration
                                                                        .getQ2Histogram()
                                                                        .getTotalCount()
                                                                        / preFailureSeconds;
                                                        double q2FailureThroughput =
                                                                        benchmarkDuration
                                                                                        .getQ2FailureHistogram()
                                                                                        .getTotalCount()
                                                                                        / failureSeconds;
                                                        double q2PostFailureThroughput =
                                                                        benchmarkDuration
                                                                                        .getQ2PostFailureHistogram()
                                                                                        .getTotalCount()
                                                                                        / postFailureSeconds;
                                                        BenchmarkResult br = new BenchmarkResult(
                                                                        benchmarkDuration
                                                                                        .getBenchmarkId(),
                                                                        pl,
                                                                        benchmarkDuration
                                                                                        .getQ1Histogram()
                                                                                        .getTotalCount(),
                                                                        benchmarkDuration
                                                                                        .getQ1FailureHistogram()
                                                                                        .getTotalCount(),
                                                                        benchmarkDuration
                                                                                        .getQ1PostFailureHistogram()
                                                                                        .getTotalCount(),
                                                                        benchmarkDuration
                                                                                        .getQ2Histogram()
                                                                                        .getTotalCount(),
                                                                        benchmarkDuration
                                                                                        .getQ2FailureHistogram()
                                                                                        .getTotalCount(),
                                                                        benchmarkDuration
                                                                                        .getQ2PostFailureHistogram()
                                                                                        .getTotalCount(),
                                                                        preFailureSeconds
                                                                                        + failureSeconds
                                                                                        + postFailureSeconds,
                                                                        q1Throughput,
                                                                        q1FailureThroughput,
                                                                        q1PostFailureThroughput,
                                                                        q2Throughput,
                                                                        q2FailureThroughput,
                                                                        q2PostFailureThroughput,
                                                                        q1_90Percentile,
                                                                        q1Failure90Percentile,
                                                                        q1PostFailure90Percentile,
                                                                        q2_90Percentile,
                                                                        q2Failure90Percentile,
                                                                        q2PostFailure90Percentile);

                                                        Gson g = new Gson();
                                                        String s = g.toJson(br);

                                                        try {
                                                                /*
                                                                 * TODO* IMPORTANT:
                                                                 * insertnewBenchmarkResult into DB
                                                                 */
                                                                q.insertBenchmarkResult(br, s,
                                                                                true);
                                                        } catch (SQLException
                                                                        | ClassNotFoundException throwables) {
                                                                throwables.printStackTrace();
                                                                Logger.error(throwables,
                                                                                "Insert of Benchmarkresult failed");
                                                                resultVerificationErrors.inc();
                                                        }
                                                } else {
                                                        for (double percentile : percentiles) {
                                                                var p = new PercentileResult(
                                                                                percentile);
                                                                if (benchmarkDuration
                                                                                .isQ1Active()) {
                                                                        long valueAtPercentile =
                                                                                        benchmarkDuration
                                                                                                        .getQ1Histogram()
                                                                                                        .getValueAtPercentile(
                                                                                                                        percentile);
                                                                        p.setQ1Latency(valueAtPercentile);
                                                                }
                                                                if (benchmarkDuration
                                                                                .isQ2Active()) {
                                                                        long valueAtPercentile =
                                                                                        benchmarkDuration
                                                                                                        .getQ2Histogram()
                                                                                                        .getValueAtPercentile(
                                                                                                                        percentile);
                                                                        p.setQ2Latency(valueAtPercentile);
                                                                }
                                                                pl.add(p);
                                                        }

                                                        double q1_90Percentile = benchmarkDuration
                                                                        .isQ1Active() ? benchmarkDuration.getQ1Histogram().getValueAtPercentile(90) / 1e6 : -1.0;
                                                        double q2_90Percentile = benchmarkDuration
                                                                        .isQ2Active() ? benchmarkDuration.getQ2Histogram().getValueAtPercentile(90) / 1e6 : -1.0;

                                                        benchmarkDuration.getStartTime();
                                                        benchmarkDuration.getEndTime();
                                                        benchmarkDuration.getQ1Histogram()
                                                                        .getTotalCount();
                                                        benchmarkDuration.getQ2Histogram()
                                                                        .getTotalCount();

                                                        double seconds = (benchmarkDuration
                                                                        .getEndTime()
                                                                        - benchmarkDuration
                                                                                        .getStartTime())
                                                                        / 1e9;
                                                        double q1Throughput = benchmarkDuration
                                                                        .getQ1Histogram()
                                                                        .getTotalCount() / seconds;
                                                        double q2Throughput = benchmarkDuration
                                                                        .getQ2Histogram()
                                                                        .getTotalCount() / seconds;

                                                        BenchmarkResult br = new BenchmarkResult(
                                                                        benchmarkDuration
                                                                                        .getBenchmarkId(),
                                                                        pl,
                                                                        benchmarkDuration
                                                                                        .getQ1Histogram()
                                                                                        .getTotalCount(),
                                                                        benchmarkDuration
                                                                                        .getQ2Histogram()
                                                                                        .getTotalCount(),
                                                                        seconds, q1Throughput,
                                                                        q2Throughput,
                                                                        q1_90Percentile,
                                                                        q2_90Percentile);

                                                        Gson g = new Gson();
                                                        String s = g.toJson(br);

                                                        try {
                                                                q.insertBenchmarkResult(br, s);
                                                        } catch (SQLException
                                                                        | ClassNotFoundException throwables) {
                                                                throwables.printStackTrace();
                                                                Logger.error(throwables,
                                                                                "Insert of Benchmarkresult failed");
                                                                resultVerificationErrors.inc();
                                                        }
                                                }
                                                durationMeasurementCounter.inc();
                                        }
                                        // Here we do some database operations, verifcation of
                                        // results and so on
                                }
                        } catch (InterruptedException ex) {
                                ex.printStackTrace();
                        }
                }
                this.shutdown.set(true);
                System.out.println("shutting down");
        }

        public void shutdown() {
                if (this.shutdown.get()) // is already shutdown
                        return;

                // set the shutdown flag to drain the queue
                this.shuttingDown.set(true);

                while (true) { // Wait till the queue is drained
                        try {
                                Thread.sleep(100);
                                if (this.shutdown.get())
                                        return;
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
        }
}
