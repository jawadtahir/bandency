package org.debs.gc2023.challenger;

// import autovalue.shaded.com.google.common.collect.AbstractMultimap;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.HdrHistogram.Histogram;
import org.debs.gc2023.FailureInjector;
import org.debs.gc2023.bandency.Batch;
import org.debs.gc2023.bandency.Benchmark;
import org.debs.gc2023.bandency.ResultQ1;
import org.debs.gc2023.bandency.ResultQ2;
import org.debs.gc2023.dal.BenchmarkDuration;
import org.debs.gc2023.dal.IQueries;
import org.debs.gc2023.dal.ToVerify;
import org.debs.gc2023.datasets.BatchIterator;
import org.rocksdb.RocksDBException;
import org.tinylog.Logger;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class BenchmarkState {
    private final ArrayBlockingQueue<ToVerify> dbInserter;
    private String token;
    private int batchSize;
    private boolean isStarted;
    private HashMap<Long, Long> pingCorrelation;
    private HashMap<Long, Long> pingCorrelationDuringFailure;
    private HashMap<Long, Long> pingCorrelationPostFailure;
    private ArrayList<Long> measurements;
    private ArrayList<Long> failureMeasurements;
    private ArrayList<Long> postFailureMeasurements;
    private List<String> toxicated;
    private static final long DELAY = 500;


    public FAILURETYPE getFailureType() {
        return failureType;
    }

    private FAILURETYPE failureType;

    public FailureInjector getFailureinjector() {
        return failureinjector;
    }

    private FailureInjector failureinjector;

    private AtomicInteger processedBatchCount = new AtomicInteger(0);

    private HashMap<Long, LatencyMeasurement> latencyCorrelation;
    private HashMap<Long, LatencyMeasurement> postFailureLatencyCorrelation;
    private HashMap<Long, LatencyMeasurement> failureLatencyCorrelation;
    private ArrayList<Long> q1measurements;


    private Histogram q1PostFailureHistogram;
    private Histogram q1Histogram;
    private Histogram q1FailureHistogram;
    private Histogram q2Histogram;
    private Histogram q2PostFailureHistogram;
    private Histogram q2FailureHistogram;

    private double averageLatency;
    private double postFailureAverageLatency;
    private double failureAverageLatency;
    private long startNanoTime;
    private long startFailureNanoTime;
    private long endPrefailureNanoTime;
    private long endFailureNanoTime;
    private long startPostfailureNanoTime;
    private BatchIterator datasource;
    private boolean q1Active;
    private boolean q2Active;
    private long benchmarkId;
    private long endNanoTime;
    private BenchmarkType benchmarkType;
    private String benchmarkName;

    public long getStartFailureNanoTime() {
        return startFailureNanoTime;
    }

    public long getEndFailureNanoTime() {
        return endFailureNanoTime;
    }

    public BenchmarkState(ArrayBlockingQueue<ToVerify> dbInserter) {
        this.dbInserter = dbInserter;
        this.averageLatency = 0.0;
        this.batchSize = -1;
        this.isStarted = false;
        this.failureType = FAILURETYPE.NONE;

        this.failureinjector = new FailureInjector();
        this.pingCorrelationDuringFailure = new HashMap<>();
        this.pingCorrelationPostFailure = new HashMap<>();
        this.pingCorrelation = new HashMap<>();
        this.measurements = new ArrayList<>();
        this.failureMeasurements = new ArrayList<>();
        this.postFailureMeasurements = new ArrayList<>();

        this.latencyCorrelation = new HashMap<>();
        this.failureLatencyCorrelation = new HashMap<>();
        this.postFailureLatencyCorrelation = new HashMap<>();
        this.q1measurements = new ArrayList<>();
        this.toxicated = new ArrayList<String>();

        averageLatency = 0.0;
        failureAverageLatency = 0.0;
        postFailureAverageLatency = 0.0;
        startNanoTime = -1;
        endNanoTime = -1;
        datasource = null;

        this.q1Active = false;
        this.q2Active = false;

        this.q1Histogram = new Histogram(3);
        this.q2Histogram = new Histogram(3);
        this.q1FailureHistogram = new Histogram(3);
        this.q1PostFailureHistogram = new Histogram(3);
        this.q2FailureHistogram = new Histogram(3);
        this.q2PostFailureHistogram = new Histogram(3);


        this.benchmarkId = -1;

        this.benchmarkType = BenchmarkType.Test;
    }

    public void setQ1(boolean contains) {
        this.q1Active = contains;
    }

    public void setQ2(boolean contains) {
        this.q2Active = contains;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setIsStarted(boolean istarted) {
        this.isStarted = istarted;
    }

    public boolean getIsStarted() {
        return this.isStarted;
    }

    public String getToken() {
        return token;
    }

    public void setBenchmarkId(long random_id) {
        this.benchmarkId = random_id;
    }

    public long getBenchmarkId() {
        return benchmarkId;
    }

    public long getEndNanoTime() {
        return endNanoTime;
    }

    public void setEndNanoTime(long endNanoTime) {
        this.endNanoTime = endNanoTime;
    }

    public BenchmarkType getBenchmarkType() {
        return benchmarkType;
    }

    public void setBenchmarkType(BenchmarkType benchmarkType) {
        this.benchmarkType = benchmarkType;
    }



    public void addLatencyTimeStamp(long random_id, long nanoTime, Boolean startedFailure,
            Boolean endFailure) {
        if (startedFailure && !endFailure) {
            pingCorrelationDuringFailure.put(random_id, nanoTime);
        } else if (endFailure) {
            pingCorrelationPostFailure.put(random_id, nanoTime);
        } else {
            pingCorrelation.put(random_id, nanoTime);

        }
    }

    public void correlatePing(long correlation_id, long nanoTime) {
        if (pingCorrelation.containsKey(correlation_id)) {
            Long sentTime = pingCorrelation.get(correlation_id);
            pingCorrelation.remove(correlation_id);
            long duration = nanoTime - sentTime;
            this.measurements.add(duration);
        } else if (pingCorrelationDuringFailure.containsKey(correlation_id)) {
            Long sentTime = pingCorrelationDuringFailure.get(correlation_id);
            pingCorrelationDuringFailure.remove(correlation_id);
            long duration = nanoTime - sentTime;
            this.failureMeasurements.add(duration);
        } else if (pingCorrelationPostFailure.containsKey(correlation_id)) {
            Long sentTime = pingCorrelationPostFailure.get(correlation_id);
            pingCorrelationPostFailure.remove(correlation_id);
            long duration = nanoTime - sentTime;
            this.postFailureMeasurements.add(duration);
        } else {

        }
    }


    public double calcAverageTransportLatency(BenchmarkPhase phase) {
        if (getFailureinjector().getPhase() == BenchmarkPhase.FAILURE_INJECTION) {
            if (this.failureMeasurements.size() > 0) {
                this.failureAverageLatency =
                        this.failureMeasurements.stream().mapToLong(a -> a).average().getAsDouble();
                return this.failureAverageLatency;
            } else {
                return 0;
            }
        } else if (getFailureinjector().getPhase() == BenchmarkPhase.POST_FAILURE_INJECTION) {
            if (this.postFailureMeasurements.size() > 0) {
                this.postFailureAverageLatency = this.postFailureMeasurements.stream()
                        .mapToLong(a -> a).average().getAsDouble();
                return this.postFailureAverageLatency;
            } else {
                return 0;
            }
        } else {
            if (this.measurements.size() > 0) {
                this.averageLatency =
                        this.measurements.stream().mapToLong(a -> a).average().getAsDouble();
                return this.averageLatency;
            } else {
                return 0;
            }
        }
    }


    public void startBenchmark(long startNanoTime) {
        this.startNanoTime = startNanoTime;
    }

    public void setDatasource(BatchIterator newDataSource) {
        this.datasource = newDataSource;
    }

    public BatchIterator getDatasource() {
        return this.datasource;
    }

    public Batch getNextBatch(long benchmarkId)
            throws InvalidProtocolBufferException, RocksDBException, InterruptedException {
        if (this.datasource == null) { // when participants ignore the last flag
            return Batch.newBuilder().setLast(true).build();
        }
        if (this.datasource.hasMoreElements()) {
            Logger.info("Has more elements: " + benchmarkId);
            Batch batch = this.datasource.nextElement();
            LatencyMeasurement lm =
                    new LatencyMeasurement(benchmarkId, batch.getSeqId(), System.nanoTime());
            this.latencyCorrelation.put(batch.getSeqId(), lm);
            return batch;
        } else {
            Logger.info("No more elements" + benchmarkId);
            this.datasource = null;
            return Batch.newBuilder().setLast(true).build();
        }
    }

    public Batch getNextBatch(long benchmarkId, int numOfBatches, Benchmark request, IQueries q)
            throws InvalidProtocolBufferException, RocksDBException, InterruptedException,
            ClassNotFoundException, SQLException {
        if (this.datasource == null) { // when participants ignore the last flag
            this.processedBatchCount.incrementAndGet();
            return Batch.newBuilder().setLast(true).build();
        }
        /*** IDENTIFY IN WHICH PHASE WE CURRENTLY ARE ***/
        Logger.info("num of batch : " + this.processedBatchCount);
        setPhaseAccordingToBatchNumber(numOfBatches, request, q);

        if (this.datasource.hasMoreElements()
                && getFailureinjector().getPhase() == BenchmarkPhase.PRE_FAILURE_INJECTION) {
            Logger.info("<pre-failure phase>Has more elements: " + benchmarkId);
            Batch batch = this.datasource.nextElement();
            LatencyMeasurement lm =
                    new LatencyMeasurement(benchmarkId, batch.getSeqId(), System.nanoTime());
            this.latencyCorrelation.put(batch.getSeqId(), lm);
            this.processedBatchCount.incrementAndGet();
            return batch;
        }

        else if (this.datasource.hasMoreElements()
                && getFailureinjector().getPhase() == BenchmarkPhase.FAILURE_INJECTION) {
            this.failureType = FAILURETYPE.LATENCY_FAILURE;
            Logger.info("<failure phase>Has more elements: " + benchmarkId);
            Batch batch = this.datasource.nextElement();
            LatencyMeasurement lm =
                    new LatencyMeasurement(benchmarkId, batch.getSeqId(), System.nanoTime());
            this.failureLatencyCorrelation.put(batch.getSeqId(), lm);
            this.processedBatchCount.incrementAndGet();
            return batch;
        }

        else if (this.datasource.hasMoreElements()
                && getFailureinjector().getPhase() == BenchmarkPhase.POST_FAILURE_INJECTION) {
            Logger.info("<post-Failure phase>Has more elements: " + benchmarkId);
            Batch batch = this.datasource.nextElement();
            LatencyMeasurement lm =
                    new LatencyMeasurement(benchmarkId, batch.getSeqId(), System.nanoTime());
            this.postFailureLatencyCorrelation.put(batch.getSeqId(), lm);
            this.processedBatchCount.incrementAndGet();
            return batch;
        }

        else {
            Logger.info("No more elements" + benchmarkId);
            this.datasource = null;
            this.processedBatchCount.incrementAndGet();
            return Batch.newBuilder().setLast(true).build();
        }
    }

    private void setPhaseAccordingToBatchNumber(int numOfBatches, Benchmark request, IQueries q)
            throws ClassNotFoundException, SQLException, InterruptedException {

        if (this.processedBatchCount.get() == ((int) (numOfBatches / 3))) {
            // retrieve VM Ip and port from database
            String[] vmInfo = computeRandomVmData(request, q);
            // parse the result
            String address = vmInfo[0].split(":")[0];
            String port = vmInfo[0].split(":")[1];
            // logging the chosen vm informations to inject failure condition into
            Logger.info("VMs Ip address" + address);
            Logger.info("VMs port" + port);
            // exclude the duration of failure injection from the total time of sending the badge
            this.endPrefailureNanoTime = System.nanoTime();
            // Injecting the Failure condition
            startLatencyInjection(DELAY, address, port, vmInfo[1]);
            this.startFailureNanoTime = System.nanoTime();
            // here we keep track of the affected vm since its faster to keep it in memory
            toxicated.add(vmInfo[0] + "/" + vmInfo[1]);
            Logger.info("Started Latency injection");
        }
        if (this.processedBatchCount.get() == ((int) (numOfBatches / 3 * 2))) {
            String info = toxicated.get(toxicated.size() - 1);// we keep last toxicated vm in memory
                                                              // for faster access
            String ip = info.split("/")[0];
            String groupName = info.split("/")[1];
            String address = ip.split(":")[0];
            String port = ip.split(":")[1];
            this.endFailureNanoTime = System.nanoTime();
            this.failureinjector.stopLatencyInjection(address, port, groupName);
            this.startPostfailureNanoTime = System.nanoTime();
            toxicated.remove(toxicated.size() - 1);
            Logger.info("stopped Latency Injection");
        }
    }

    public void startLatencyInjection(long delay, String address, String port, String groupname) {
        this.failureType = FAILURETYPE.LATENCY_FAILURE;
        this.failureinjector.startLatencyInjection(delay, address, port, groupname);
    }

    /**
     * @throws InterruptedException
     * @throws SQLException
     * @throws ClassNotFoundException
     *****************************************************************************************************************/

    private String[] computeRandomVmData(Benchmark request, IQueries q)
            throws ClassNotFoundException, SQLException, InterruptedException {
        String lastByteString;

        AtomicReference<String> adrContainer = new AtomicReference<>();
        AtomicReference<String> nameContainer = new AtomicReference<>();
        // here we choose a random vm to shut down

        nameContainer.set(q.getGroupNameFromToken(this.getToken()));
        List<String> vmAdrs = q.getVirtualMachineInfo(this.getToken());
        if (vmAdrs != null && !vmAdrs.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(vmAdrs.size());
            adrContainer.set(vmAdrs.get(randomIndex));
            Logger.info("the randomly chosen addr is : " + adrContainer.get() + "for the gorup : "
                    + nameContainer.get());
        } else {
            throw new InterruptedException(
                    "still no VM registred in DATABASE for this group. please inform DEBS organizers.");
        }


        String[] res = {adrContainer.get(), nameContainer.get()};
        return res;
    }

    public void resultsQ1(ResultQ1 request, long nanoTime, String s) {
        if (latencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = latencyCorrelation.get(request.getBatchSeqId());
            lm.setQ1Results(nanoTime, request);
            q1Histogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                latencyCorrelation.remove(request.getBatchSeqId());
            }
        } else if (failureLatencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = failureLatencyCorrelation.get(request.getBatchSeqId());
            lm.setQ1FailureResults(nanoTime, request);
            q1FailureHistogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                failureLatencyCorrelation.remove(request.getBatchSeqId());
            }
        } else if (postFailureLatencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = postFailureLatencyCorrelation.get(request.getBatchSeqId());
            lm.setQ1PostFailureResults(nanoTime, request);
            q1PostFailureHistogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                postFailureLatencyCorrelation.remove(request.getBatchSeqId());
            }
        }
    }

    public void resultsQ1(ResultQ1 request, long nanoTime) {
        if (latencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = latencyCorrelation.get(request.getBatchSeqId());
            lm.setQ1Results(nanoTime, request);
            q1Histogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                latencyCorrelation.remove(request.getBatchSeqId());
            }
        }
    }

    public void resultsQ2(ResultQ2 request, long nanoTime) {
        if (latencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = latencyCorrelation.get(request.getBatchSeqId());
            lm.setQ2Results(nanoTime, request);
            q2Histogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                latencyCorrelation.remove(request.getBatchSeqId());
            }
        }
    }

    /* SAME As resultsQ1 */
    public void resultsQ2(ResultQ2 request, long nanoTime, String s) {
        if (latencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = latencyCorrelation.get(request.getBatchSeqId());
            lm.setQ2Results(nanoTime, request);
            q2Histogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                latencyCorrelation.remove(request.getBatchSeqId());
            }
        } else if (failureLatencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = failureLatencyCorrelation.get(request.getBatchSeqId());
            lm.setQ2FailureResults(nanoTime, request);
            q2FailureHistogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                failureLatencyCorrelation.remove(request.getBatchSeqId());
            }
        } else if (postFailureLatencyCorrelation.containsKey(request.getBatchSeqId())) {
            LatencyMeasurement lm = postFailureLatencyCorrelation.get(request.getBatchSeqId());
            lm.setQ2PostFailureResults(nanoTime, request);
            q2PostFailureHistogram.recordValue(nanoTime - lm.getStartTime());
            if (isfinished(lm)) {
                this.dbInserter.add(new ToVerify(lm));
                postFailureLatencyCorrelation.remove(request.getBatchSeqId());
            }
        }
    }


    private boolean isfinished(LatencyMeasurement lm) {
        if ((this.q1Active == lm.hasQ1Results()) && (this.q2Active == lm.hasQ2Results())) {
            return true;
        }
        return false;
    }

    public void endBenchmark(long benchmarkId, long endTime) {
        this.endNanoTime = endTime;

        BenchmarkDuration bd = new BenchmarkDuration(benchmarkId, this.startNanoTime, endTime,
                this.averageLatency, q1Histogram, q2Histogram, this.q1Active, this.q2Active);
        this.dbInserter.add(new ToVerify(bd));
        this.processedBatchCount.set(0);
    }

    public void endBenchmark(long benchmarkId, long endTime, String s) {
        this.endNanoTime = endTime;

        BenchmarkDuration bd = new BenchmarkDuration(benchmarkId, this.startNanoTime,
                this.endPrefailureNanoTime, this.startFailureNanoTime, this.endFailureNanoTime,
                this.startPostfailureNanoTime, endTime, this.averageLatency, q1Histogram,
                q1FailureHistogram, q1PostFailureHistogram, q2Histogram, q2FailureHistogram,
                q2PostFailureHistogram, this.q1Active, this.q2Active);
        this.dbInserter.add(new ToVerify(bd));
        this.processedBatchCount.set(0);
    }


    @Override
    public String toString() {
        if (benchmarkType == BenchmarkType.fte) {
            return "BenchmarkState{" + "dbInserter=" + dbInserter.size() + ", token='" + token
                    + '\'' + ", batchSize=" + batchSize + ", pingCorrelationPreFailure="
                    + pingCorrelation.size() + ", pingCorrelationDuringFailure="
                    + pingCorrelationDuringFailure.size() + ", pingCorrelationPostFailure="
                    + pingCorrelationPostFailure.size() + ", measurements=" + measurements.size()
                    + ", FailureMeasurements=" + failureMeasurements.size()
                    + ", PostFailuremeasurements=" + postFailureMeasurements.size()
                    + ", latencyCorrelation=" + latencyCorrelation.size()
                    + ", failureLatencyCorrelation=" + failureLatencyCorrelation.size()
                    + ", postFailurelatencyCorrelation=" + postFailureLatencyCorrelation.size()
                    + ", q1measurements=" + q1measurements.size() + ", averageLatency="
                    + averageLatency + ", DuringFailureAverageLatency=" + failureAverageLatency
                    + ", postFailureAverageLatency=" + postFailureAverageLatency
                    + ", startNanoTime=" + startNanoTime + ", q1Active=" + q1Active + ", q2Active="
                    + q2Active + ", benchmarkId=" + benchmarkId + ", endNanoTime=" + endNanoTime
                    + ", benchmarkType=" + benchmarkType + ", benchmarkName=" + benchmarkName
                    + ", FailureType=" + this.failureType + '}';
        } else {
            return "BenchmarkState{" + "dbInserter=" + dbInserter.size() + ", token='" + token
                    + '\'' + ", batchSize=" + batchSize + ", pingCorrelation="
                    + pingCorrelation.size() + ", measurements=" + measurements.size()
                    + ", latencyCorrelation=" + latencyCorrelation.size() + ", q1measurements="
                    + q1measurements.size() + ", averageLatency=" + averageLatency
                    + ", startNanoTime=" + startNanoTime + ", q1Active=" + q1Active + ", q2Active="
                    + q2Active + ", benchmarkId=" + benchmarkId + ", endNanoTime=" + endNanoTime
                    + ", benchmarkType=" + benchmarkType + ", benchmarkName=" + benchmarkName + '}';
        }
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }
}
