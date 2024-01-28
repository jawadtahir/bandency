package org.debs.gc2023;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
// import eu.rekawek.toxiproxy.Proxy;
import io.grpc.Grpc;
import io.grpc.ServerCall;
import io.grpc.Status;
import io.grpc.netty.shaded.io.netty.internal.tcnative.SSLSession;
import io.grpc.stub.StreamObserver;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.debs.gc2023.bandency.*;
import org.debs.gc2023.bandency.ChallengerGrpc.ChallengerImplBase;
import org.debs.gc2023.challenger.BenchmarkPhase;
import org.debs.gc2023.challenger.BenchmarkState;
import org.debs.gc2023.challenger.BenchmarkType;
import org.debs.gc2023.challenger.FAILURETYPE;
import org.debs.gc2023.dal.IQueries;
import org.debs.gc2023.dal.ToVerify;
import org.debs.gc2023.datasets.BatchIterator;
import org.debs.gc2023.datasets.IDataStore;
import org.rocksdb.RocksDBException;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import io.grpc.Context;
import io.grpc.ServerCall;


public class ChallengerServer extends ChallengerImplBase {
    private final ArrayBlockingQueue<ToVerify> dbInserter;
    private final IQueries q;
    private final int durationEvaluationMinutes;
    private final Random random;
    final private ConcurrentHashMap<Long, BenchmarkState> benchmark;
    private IDataStore store;
    private BenchmarkConfiguration currentBenchmark;
    private boolean removedFailure = false;

    public ChallengerServer(IDataStore store, ArrayBlockingQueue<ToVerify> dbInserter, IQueries q,
            int durationEvaluationMinutes) {
        this.store = store;
        this.dbInserter = dbInserter;
        this.q = q;
        this.durationEvaluationMinutes = durationEvaluationMinutes;
        benchmark = new ConcurrentHashMap<>();
        random = new Random(System.nanoTime());
    }

    static final Counter errorCounter =
            Counter.build().name("errors").help("unforseen errors").register();

    static final Counter createNewBenchmarkCounter = Counter.build().name("createNewBenchmark")
            .help("calls to createNewBenchmark methods").register();

    @Override
    public void createNewBenchmark(BenchmarkConfiguration request,
            StreamObserver<Benchmark> responseObserver) {
        String token = request.getToken();

        try {
            if (!q.checkIfGroupExists(token)) {
                Status status = Status.FAILED_PRECONDITION.withDescription("token invalid");
                responseObserver.onError(status.asException());
                responseObserver.onCompleted();
                return;
            }
        } catch (SQLException | ClassNotFoundException | InterruptedException throwables) {
            Logger.error(throwables);
            errorCounter.inc();

            Status status = Status.INTERNAL
                    .withDescription("database offline - plz. inform the challenge organizers");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        if (request.getQueriesList().size() < 1) {
            Logger.info("no query selected: " + request.getToken());

            Status status = Status.FAILED_PRECONDITION.withDescription("no query selected");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        if (!isValid(request.getBenchmarkType())) {
            Logger.info("different BenchmarkType set: " + request.getBenchmarkType());

            Status status = Status.FAILED_PRECONDITION.withDescription("unsupported benchmarkType");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }
        this.currentBenchmark = request;
        BenchmarkType bt = BenchmarkType.Test;
        int batchSize = 1_000;

        if (request.getBenchmarkType().equalsIgnoreCase("test")) {
            bt = BenchmarkType.Test;
            batchSize = 1_000;
        } else if (request.getBenchmarkType().equalsIgnoreCase("verification")) {
            bt = BenchmarkType.Verification;
        } else if (request.getBenchmarkType().equalsIgnoreCase("fte")) {
            bt = BenchmarkType.fte;
            batchSize = 1_000;
        } else if (request.getBenchmarkType().equalsIgnoreCase("evaluation")) {
            bt = BenchmarkType.Evaluation;
            batchSize = 1_000;
        }

        // Save this benchmarkname to database
        String benchmarkName = request.getBenchmarkName();
        long benchmarkId = Math.abs(random.nextLong());

        try {
            UUID groupId = q.getGroupIdFromToken(token);
            q.insertBenchmarkStarted(benchmarkId, groupId, benchmarkName, batchSize, bt);

        } catch (SQLException | ClassNotFoundException | InterruptedException throwables) {
            Logger.error(throwables);
            errorCounter.inc();

            Status status = Status.INTERNAL.withDescription(
                    "plz. inform the challenge organisers - database not reachable");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        BenchmarkState bms = new BenchmarkState(this.dbInserter);
        bms.setToken(token);
        bms.setBenchmarkId(benchmarkId);
        bms.setToken(token);
        bms.setBenchmarkType(bt);
        bms.setBenchmarkName(benchmarkName);

        bms.setQ1(request.getQueriesList().contains(Query.Q1));
        bms.setQ2(request.getQueriesList().contains(Query.Q2));

        Instant stopTime = Instant.now().plus(durationEvaluationMinutes, ChronoUnit.MINUTES);

        if (bt == BenchmarkType.Evaluation || bt == BenchmarkType.fte) {
            var bi = new BatchIterator(this.store, stopTime);
            bms.setDatasource(bi);
        } else {
            // for the time being, there is no difference in the dataset
            var bi = new BatchIterator(this.store, stopTime);
            bms.setDatasource(bi);
        }
        Logger.info("Ready for benchmark: " + bms.toString());

        this.benchmark.put(benchmarkId, bms);



        Benchmark bm = Benchmark.newBuilder().setId(benchmarkId).build();
        responseObserver.onNext(bm);
        responseObserver.onCompleted();
        createNewBenchmarkCounter.inc();
    }



    static boolean isValid(String bmType) {
        return true;
    }

    static final Counter startBenchmarkCounter = Counter.build().name("startBenchmark")
            .help("calls to startBenchmark methods").register();

    @Override
    public void startBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        if (!this.benchmark.containsKey(request.getId())) {
            Status status = Status.FAILED_PRECONDITION.withDescription("Benchmark not created");

            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            b.setIsStarted(true);
            b.startBenchmark(System.nanoTime());
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        startBenchmarkCounter.inc();
    }

    static final Histogram batchReadLatency = Histogram.build().name("batchReadLatency_seconds")
            .help("Batch read latency in seconds.").register();

    static final Counter nextBatchTest = Counter.build().name("nextMessage_test")
            .help("calls to nextMessage methods with test").register();

    static final Counter nextBatchValidation = Counter.build().name("nextMessage_validation")
            .help("calls to nextMessage methods with validation").register();

    @Override
    public synchronized void nextBatch(Benchmark request, StreamObserver<Batch> responseObserver) {

        if (!this.benchmark.containsKey(request.getId())) {
            Status status = Status.FAILED_PRECONDITION.withDescription("Benchmark not created");

            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        if (!this.benchmark.get(request.getId()).getIsStarted()) {
            Status status = Status.FAILED_PRECONDITION
                    .withDescription("Benchmark not started, call startBenchmark first");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        AtomicReference<Batch> batchRef = new AtomicReference<>();
        AtomicReference<BenchmarkState> bms = new AtomicReference<>();
        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            try {
                if (b.getBenchmarkType() == BenchmarkType.Evaluation) { // this comes from memory
                                                                        // and is too fast
                    bms.set(b);
                    batchRef.set(b.getNextBatch(request.getId()));
                    nextBatchValidation.inc();
                    nextBatchValidation.inc();
                } else if (b.getBenchmarkType() == BenchmarkType.fte) {
                    bms.set(b);
                    batchRef.set(b.getNextBatch(request.getId(), this.store.BatchCount(), request,
                            this.q));
                    nextBatchValidation.inc();
                    nextBatchValidation.inc();
                } else {
                    Histogram.Timer batchReadTimer = batchReadLatency.startTimer();
                    batchRef.set(b.getNextBatch(request.getId()));
                    batchReadTimer.observeDuration();
                    nextBatchTest.inc();
                }
            } catch (InvalidProtocolBufferException | RocksDBException e) {
                e.printStackTrace();
                Status status = Status.INTERNAL.withDescription(
                        "Could not get next batch, plz. let the DEBS challenge organizers know that they should fix this bug, .. IMMEDIATELY. thanks!");
                responseObserver.onError(status.asException());
                responseObserver.onCompleted();
            } catch (SQLException | ClassNotFoundException | InterruptedException throwables) {
                Logger.error(throwables);

                Status status = Status.INTERNAL.withDescription(
                        "Vms ip could not be retrieved, please let the DEBS challenge organizers know that they should fix this bug, .. IMMEDIATELY. thanks!");
                responseObserver.onError(status.asException());
                responseObserver.onCompleted();
            }
            return b;
        });

        Batch acquired_batch = batchRef.getAcquire();
        if (acquired_batch == null) {
            Status status = Status.INTERNAL.withDescription("Could not get next batch");

            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        } else {
            if (bms.get().getFailureinjector().getPhase() == BenchmarkPhase.FAILURE_INJECTION) {

                responseObserver.onNext(acquired_batch);
                responseObserver.onCompleted();
            } else {
                responseObserver.onNext(acquired_batch);
                responseObserver.onCompleted();
            }

        }
    }

    /*********************************************************************************************************************************** */

    private String[] computeRandomVmData(Benchmark request,
            StreamObserver<Batch> responseObserver) {
        String lastByteString;

        AtomicReference<String> adrContainer = new AtomicReference<>();
        AtomicReference<String> nameContainer = new AtomicReference<>();
        // here we choose a random vm to shut down
        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            try {
                nameContainer.set(q.getGroupNameFromToken(b.getToken()));
                List<String> vmAdrs = q.getVirtualMachineInfo(b.getToken());
                for (String internalAdrs : vmAdrs)
                    Logger.info("Iterating the list : Internal Address: " + internalAdrs);
                if (vmAdrs != null && !vmAdrs.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(vmAdrs.size());
                    adrContainer.set(vmAdrs.get(randomIndex));
                    Logger.info("the randomly chosen addr is : " + adrContainer.get());
                } else {
                    throw new InterruptedException(
                            "still no VM registred in DATABASE for this group. please inform DEBS organizers.");
                }
            } catch (SQLException | ClassNotFoundException | InterruptedException throwables) {
                Logger.error(throwables);
                errorCounter.inc();

                Status status = Status.INTERNAL.withDescription(
                        "choosing random vm to add latency : Vms ip could not be retrieved please contact DEBS organizers.");
                responseObserver.onError(status.asException());
                responseObserver.onCompleted();
            }

            return b;
        });

        String[] res = {adrContainer.get(), nameContainer.get()};
        return res;
    }



    /*********************************************************************************************************************************** */



    static final Counter resultQ1Counter =
            Counter.build().name("resultQ1").help("calls to resultQ1 methods").register();

    @Override
    public void resultQ1(ResultQ1 request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        if (!this.benchmark.containsKey(request.getBenchmarkId())) {
            Status status = Status.FAILED_PRECONDITION.withDescription("Benchmark not created");

            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        if (!this.benchmark.get(request.getBenchmarkId()).getIsStarted()) {
            Status status = Status.FAILED_PRECONDITION
                    .withDescription("Benchmark not started, call startBenchmark first");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        this.benchmark.computeIfPresent(request.getBenchmarkId(), (k, b) -> {
            if (b.getBenchmarkType() == BenchmarkType.fte) {
                b.resultsQ1(request, nanoTime, "");
            } else {
                b.resultsQ1(request, nanoTime);
            }
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        resultQ1Counter.inc();
    }

    static final Counter resultQ2Counter =
            Counter.build().name("resultQ2").help("calls to resultQ2 methods").register();

    @Override
    public void resultQ2(ResultQ2 request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        if (!this.benchmark.containsKey(request.getBenchmarkId())) {
            Status status = Status.FAILED_PRECONDITION.withDescription("Benchmark not started");

            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        if (!this.benchmark.get(request.getBenchmarkId()).getIsStarted()) {
            Status status = Status.FAILED_PRECONDITION
                    .withDescription("Benchmark not started, call startBenchmark first");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        this.benchmark.computeIfPresent(request.getBenchmarkId(), (k, b) -> {
            if (b.getBenchmarkType() == BenchmarkType.fte) {
                b.resultsQ2(request, nanoTime, "");
            } else {
                b.resultsQ2(request, nanoTime);
            }
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        resultQ2Counter.inc();
    }


    static final Counter endBenchmarkCounter =
            Counter.build().name("endBenchmark").help("calls to endBenchmark methods").register();

    @Override
    public void endBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        if (!this.benchmark.containsKey(request.getId())) {
            Status status = Status.FAILED_PRECONDITION.withDescription("Benchmark not started");

            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        if (!this.benchmark.get(request.getId()).getIsStarted()) {
            Status status = Status.FAILED_PRECONDITION
                    .withDescription("Benchmark not started, call startBenchmark first");
            responseObserver.onError(status.asException());
            responseObserver.onCompleted();
            return;
        }

        AtomicBoolean found = new AtomicBoolean(false);
        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            if (b.getBenchmarkType() == BenchmarkType.fte) {
                b.endBenchmark(request.getId(), nanoTime, "");
            } else {
                b.endBenchmark(request.getId(), nanoTime);
            }
            found.set(true);
            Logger.info("Ended benchmark: " + b.toString());
            return b;
        });

        if (found.get()) {
            this.benchmark.remove(request.getId());
        }


        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        endBenchmarkCounter.inc();
    }

}
