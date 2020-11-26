package de.tum.i13;

import com.google.protobuf.Empty;
import de.tum.i13.bandency.*;
import de.tum.i13.challenger.BenchmarkState;
import de.tum.i13.dal.ToVerify;
import de.tum.i13.datasets.airquality.AirqualityDataset;
import de.tum.i13.datasets.location.LocationDataset;
import io.grpc.stub.StreamObserver;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.tinylog.Logger;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ChallengerServer extends ChallengerGrpc.ChallengerImplBase {
    final private LocationDataset ld;
    private final AirqualityDataset ad;
    private final ArrayBlockingQueue<ToVerify> dbInserter;


    final private ConcurrentHashMap<Long, BenchmarkState> benchmark;

    public ChallengerServer(LocationDataset ld, AirqualityDataset ad, ArrayBlockingQueue<ToVerify> dbInserter) {
        this.ld = ld;
        this.ad = ad;
        this.dbInserter = dbInserter;
        benchmark = new ConcurrentHashMap<>();
    }

    static final Counter getLocationsCounter = Counter.build()
            .name("getLocations")
            .help("calls to getLocations methods")
            .register();

    @Override
    public void getLocations(Empty request, StreamObserver<Locations> responseObserver) {
        responseObserver.onNext(ld.getAllLocations());
        responseObserver.onCompleted();

        getLocationsCounter.inc();
    }

    static final Counter createNewBenchmarkCounter = Counter.build()
            .name("createNewBenchmark")
            .help("calls to createNewBenchmark methods")
            .register();

    @Override
    public void createNewBenchmark(BenchmarkConfiguration request, StreamObserver<Benchmark> responseObserver) {
        //Verify the token that it is actually allowed to start a benchmark
        //TODO: go to database, get groupname
        String token = request.getToken();

        //Only a certain configuration for the batchsize is allowed, check if this is allowed
        //TODO: precompute the batchsizes
        int batchSize = request.getBatchSize();
        if(batchSize > 20_000) {
            Logger.info("no benchmark selected: " + request.getToken());
            responseObserver.onError(new Exception("batchsize to large"));
            responseObserver.onCompleted();
            return;
        }

        batchSizeHistogram.observe(batchSize);

        if(request.getQueriesList().size() < 1) {
            Logger.info("no benchmark selected: " + request.getToken());
            responseObserver.onError(new Exception("no benchmark selected"));
            responseObserver.onCompleted();
            return;
        }

        //Save this benchmarkname to database
        //TODO:
        String benchmarkName = request.getBenchmarkName();
        long benchmarkId = new Random().nextLong();

        BenchmarkState bms = new BenchmarkState(this.dbInserter);
        bms.setToken(token);
        bms.setBenchmarkId(benchmarkId);
        bms.setToken(token);
        bms.setBatchSize(batchSize);

        bms.setQ1(request.getQueriesList().contains(BenchmarkConfiguration.Query.Q1));
        bms.setQ2(request.getQueriesList().contains(BenchmarkConfiguration.Query.Q2));


        Logger.info("Ready for benchmark: " + bms.toString());

        this.benchmark.put(benchmarkId, bms);

        Benchmark bm = Benchmark.newBuilder()
                .setId(benchmarkId)
                .build();

        responseObserver.onNext(bm);
        responseObserver.onCompleted();

        createNewBenchmarkCounter.inc();
    }

    static final Counter initializeLatencyMeasuringCounter = Counter.build()
            .name("initializeLatencyMeasuring")
            .help("calls to initializeLatencyMeasuring methods")
            .register();

    @Override
    public void initializeLatencyMeasuring(Benchmark request, StreamObserver<Ping> responseObserver) {
        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        long random_id = new Random().nextLong();
        Ping ping = Ping.newBuilder()
                .setBenchmarkId(request.getId())
                .setCorrelationId(random_id)
                .build();

        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            b.addLatencyTimeStamp(random_id, System.nanoTime());
            return b;
        });

        responseObserver.onNext(ping);
        responseObserver.onCompleted();

        initializeLatencyMeasuringCounter.inc();
    }

    static final Counter measureCounter = Counter.build()
            .name("measure")
            .help("calls to measure methods")
            .register();

    @Override
    public void measure(Ping request, StreamObserver<Ping> responseObserver) {
        if(!this.benchmark.containsKey(request.getBenchmarkId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        long current_time = System.nanoTime();
        AtomicReference<Ping> ping = new AtomicReference<>();
        this.benchmark.computeIfPresent(request.getBenchmarkId(), (k, b) -> {
            b.correlatePing(request.getCorrelationId(), current_time);

            long random_id = new Random().nextLong();
            Ping local = Ping.newBuilder()
                    .setCorrelationId(random_id)
                    .setBenchmarkId(request.getBenchmarkId())
                    .build();

            ping.set(local);
            b.addLatencyTimeStamp(random_id, System.nanoTime());
            return b;
        });

        Ping acquiredPing = ping.getAcquire();
        if(acquiredPing == null) {
            responseObserver.onError(new Exception(""));
        }
        responseObserver.onNext(ping.get());
        responseObserver.onCompleted();

        measureCounter.inc();
    }


    static final Counter endMeasurementCounter = Counter.build()
            .name("endMeasurement")
            .help("calls to endMeasurement methods")
            .register();

    static final Histogram measurementHistogram = Histogram.build()
            .name("clientLatency")
            .help("measurements of client latency")
            .register();

    @Override
    public void endMeasurement(Ping request, StreamObserver<Empty> responseObserver) {
        if(!this.benchmark.containsKey(request.getBenchmarkId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        this.benchmark.computeIfPresent(request.getBenchmarkId(), (k, b)-> {
            long correlation_id = request.getCorrelationId();
            b.correlatePing(correlation_id, System.nanoTime());
            double v = b.calcAverageTransportLatency();
            if(v > 0) {
                v /= 1_000_000;
                measurementHistogram.observe(v);
            }

            Logger.debug("average latency: " + v + "ms");
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        endMeasurementCounter.inc();
    }


    static final Counter startBenchmarkCounter = Counter.build()
            .name("startBenchmark")
            .help("calls to startBenchmark methods")
            .register();

    @Override
    public void startBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            b.startBenchmark(System.nanoTime());
            b.setDatasource(ad.newDataSource(b.getBatchSize()));
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        startBenchmarkCounter.inc();
    }

    static final Histogram batchReadLatency = Histogram.build()
            .name("batchReadLatency_seconds")
            .help("Batch read latency in seconds.").register();

    static final Counter nextMessageCounter = Counter.build()
            .name("nextMessage")
            .help("calls to nextMessage methods")
            .register();

    static final Histogram batchSizeHistogram = Histogram.build()
            .name("batchsize")
            .help("batchsize of benchmark")
            .linearBuckets(0.0, 1_000.0, 21)
            .create()
            .register();

    @Override
    public void nextMessage(Benchmark request, StreamObserver<Batch> responseObserver) {
        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        AtomicReference<Batch> batchRef = new AtomicReference<>();;
        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {

            Histogram.Timer batchReadTimer = batchReadLatency.startTimer();
            batchRef.set(b.getNextBatch(request.getId()));
            batchReadTimer.observeDuration();

            //Additionally record the batchsize to put the latency into perspective
            batchSizeHistogram.observe(b.getBatchSize());

            return b;
        });

        Batch acquired_batch = batchRef.getAcquire();
        if(acquired_batch == null) {
            responseObserver.onError(new Exception("Could not get next batch"));
        } else {
            responseObserver.onNext(acquired_batch);
            responseObserver.onCompleted();
        }

        nextMessageCounter.inc();
    }


    static final Counter resultQ1Counter = Counter.build()
            .name("resultQ1")
            .help("calls to resultQ1 methods")
            .register();

    @Override
    public void resultQ1(ResultQ1 request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        if(!this.benchmark.containsKey(request.getBenchmarkId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        this.benchmark.computeIfPresent(request.getBenchmarkId(), (k, b) -> {
            b.resultsQ1(request, nanoTime);
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        resultQ1Counter.inc();
    }

    static final Counter resultQ2Counter = Counter.build()
            .name("resultQ2")
            .help("calls to resultQ2 methods")
            .register();

    @Override
    public void resultQ2(ResultQ2 request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        if(!this.benchmark.containsKey(request.getBenchmarkId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        this.benchmark.computeIfPresent(request.getBenchmarkId(), (k, b) -> {
            b.resultsQ2(request, nanoTime);
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        resultQ2Counter.inc();
    }


    static final Counter endBenchmarkCounter = Counter.build()
            .name("endBenchmark")
            .help("calls to endBenchmark methods")
            .register();
    @Override
    public void endBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            b.endBenchmark(request.getId(), nanoTime);

            Logger.info("Ended benchmark: " + b.toString());
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

        endBenchmarkCounter.inc();
    }
}
