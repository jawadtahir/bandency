package de.tum.i13;

import com.google.protobuf.Empty;
import de.tum.i13.bandency.*;
import de.tum.i13.challenger.BenchmarkState;
import de.tum.i13.dal.ToVerify;
import de.tum.i13.datasets.airquality.AirqualityDataset;
import de.tum.i13.datasets.location.LocationDataset;
import io.grpc.stub.StreamObserver;
import org.tinylog.Logger;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ChallengerServer extends ChallengerGrpc.ChallengerImplBase {
    private final AtomicInteger reqcounter;

    final private LocationDataset ld;
    private final AirqualityDataset ad;
    private final ArrayBlockingQueue<ToVerify> dbInserter;

    final private ConcurrentHashMap<Long, BenchmarkState> benchmark;

    public ChallengerServer(LocationDataset ld, AirqualityDataset ad, ArrayBlockingQueue<ToVerify> dbInserter) {
        this.ld = ld;
        this.ad = ad;
        this.dbInserter = dbInserter;
        benchmark = new ConcurrentHashMap<>();

        reqcounter = new AtomicInteger(0);
    }

    @Override
    public void getLocations(Empty request, StreamObserver<Locations> responseObserver) {
        int req = reqcounter.incrementAndGet();
        Logger.debug("getLocations - cnt: " + req);
        responseObserver.onNext(ld.getAllLocations());
        responseObserver.onCompleted();
    }

    @Override
    public void createNewBenchmark(BenchmarkConfiguration request, StreamObserver<Benchmark> responseObserver) {
        int req = reqcounter.incrementAndGet();
        Logger.debug("createNewBenchmark - cnt: " + req);

        //Verify the token that it is actually allowed to start a benchmark
        //TODO: go to database, get groupname
        String token = request.getToken();

        //Only a certain configuration for the batchsize is allowed, check if this is allowed
        //TODO: precompute the batchsizes
        int batchSize = request.getBatchSize();

        //Save this benchmarkname to database
        //TODO:
        String benchmarkName = request.getBenchmarkName();
        long random_id = new Random().nextLong();

        BenchmarkState bms = new BenchmarkState(this.dbInserter);
        bms.setToken(token);
        bms.setBatchSize(batchSize);
        bms.setQ1(request.getQueriesList().contains(BenchmarkConfiguration.Query.Q1));
        bms.setQ2(request.getQueriesList().contains(BenchmarkConfiguration.Query.Q2));

        this.benchmark.put(random_id, bms);

        Benchmark bm = Benchmark.newBuilder()
                .setId(random_id)
                .build();

        responseObserver.onNext(bm);
        responseObserver.onCompleted();
    }

    @Override
    public void initializeLatencyMeasuring(Benchmark request, StreamObserver<Ping> responseObserver) {
        int req = reqcounter.incrementAndGet();
        Logger.debug("initializeLatencyMeasuring - cnt: " + req);

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
    }

    @Override
    public void measure(Ping request, StreamObserver<Ping> responseObserver) {
        int req = reqcounter.incrementAndGet();
        Logger.debug("measure - cnt: " + req);

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
    }

    @Override
    public void endMeasurement(Ping request, StreamObserver<Empty> responseObserver) {
        int req = reqcounter.incrementAndGet();
        Logger.debug("endMeasurement - cnt: " + req);

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
            }
            Logger.debug("average latency: " + v + "ms");
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void startBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        int req = reqcounter.incrementAndGet();
        Logger.debug("startBenchmark - cnt: " + req);

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
    }

    @Override
    public void nextMessage(Benchmark request, StreamObserver<Batch> responseObserver) {
        int req = reqcounter.incrementAndGet();
        Logger.debug("nextMessage - cnt: " + req);

        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        AtomicReference<Batch> batchRef = new AtomicReference<>();;
        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            batchRef.set(b.getNextBatch(request.getId()));
            return b;
        });

        Batch acquired_batch = batchRef.getAcquire();
        if(acquired_batch == null) {
            responseObserver.onError(new Exception("Could not get next batch"));
        } else {
            responseObserver.onNext(acquired_batch);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void resultQ1(ResultQ1 request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        int req = reqcounter.incrementAndGet();
        Logger.debug("processed - cnt: " + req);

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
    }

    @Override
    public void resultQ2(ResultQ2 request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        int req = reqcounter.incrementAndGet();
        Logger.debug("processed - cnt: " + req);

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

    }

    @Override
    public void endBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        long nanoTime = System.nanoTime();

        int req = reqcounter.incrementAndGet();
        Logger.debug("endBenchmark - cnt: " + req);

        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        this.benchmark.computeIfPresent(request.getId(), (k, b) -> {
            b.endBenchmark(request.getId(), nanoTime);
            return b;
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
