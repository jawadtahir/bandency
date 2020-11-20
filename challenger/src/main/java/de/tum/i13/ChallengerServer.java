package de.tum.i13;

import com.google.protobuf.Empty;
import de.tum.i13.bandency.*;
import de.tum.i13.challenger.BenchmarkState;
import de.tum.i13.datasets.airquality.AirqualityDataset;
import de.tum.i13.datasets.location.LocationDataset;
import io.grpc.stub.StreamObserver;
import org.tinylog.Logger;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;

public class ChallengerServer extends ChallengerGrpc.ChallengerImplBase {
    final private LocationDataset ld;
    private final AirqualityDataset ad;

    final private HashMap<Long, BenchmarkState> benchmark;

    public ChallengerServer(LocationDataset ld, AirqualityDataset ad) {
        this.ld = ld;
        this.ad = ad;
        benchmark = new HashMap<>();
    }

    @Override
    public void getLocations(Empty request, StreamObserver<Locations> responseObserver) {
        Logger.debug("getLocations");
        responseObserver.onNext(ld.getAllLocations());
        responseObserver.onCompleted();
    }

    @Override
    public void createNewBenchmark(BenchmarkConfiguration request, StreamObserver<Benchmark> responseObserver) {
        Logger.debug("createNewBenchmark");

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
        BenchmarkState bms = new BenchmarkState();
        bms.setToken(token);
        bms.setBatchSize(batchSize);

        this.benchmark.put(random_id, bms);

        Benchmark bm = Benchmark.newBuilder()
                .setId(random_id)
                .build();

        responseObserver.onNext(bm);
        responseObserver.onCompleted();
    }

    @Override
    public void initializeLatencyMeasuring(Benchmark request, StreamObserver<Ping> responseObserver) {
        Logger.debug("initializeLatencyMeasuring");

        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        long random_id = new Random().nextLong();
        Ping ping = Ping.newBuilder()
                .setBenchmarkId(request.getId())
                .setCorrelationId(random_id)
                .build();

        BenchmarkState benchmarkState = this.benchmark.get(request.getId());
        benchmarkState.addLatencyTimeStamp(random_id, System.nanoTime());

        responseObserver.onNext(ping);
        responseObserver.onCompleted();
    }

    @Override
    public void measure(Ping request, StreamObserver<Ping> responseObserver) {
        Logger.debug("measure");

        if(!this.benchmark.containsKey(request.getBenchmarkId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        BenchmarkState benchmarkState = this.benchmark.get(request.getBenchmarkId());

        long correlation_id = request.getCorrelationId();
        benchmarkState.correlatePing(correlation_id, System.nanoTime());

        long random_id = new Random().nextLong();
        Ping ping = Ping.newBuilder()
                .setCorrelationId(random_id)
                .setBenchmarkId(request.getBenchmarkId())
                .build();
        benchmarkState.addLatencyTimeStamp(random_id, System.nanoTime());

        responseObserver.onNext(ping);
        responseObserver.onCompleted();
    }

    @Override
    public void endMeasurement(Ping request, StreamObserver<Empty> responseObserver) {
        Logger.debug("endMeasurement");

        if(!this.benchmark.containsKey(request.getBenchmarkId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        BenchmarkState benchmarkState = this.benchmark.get(request.getBenchmarkId());

        long correlation_id = request.getCorrelationId();
        benchmarkState.correlatePing(correlation_id, System.nanoTime());
        double v = benchmarkState.calcAverageTransportLatency();
        if(v > 0) {
            v /= 1_000_000;
        }
        Logger.debug("average latency: " + v + "ms");

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void startBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        Logger.debug("startBenchmark");

        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        BenchmarkState benchmarkState = this.benchmark.get(request.getId());
        benchmarkState.startBenchmark(System.nanoTime());
        benchmarkState.setDatasource(ad.newDataSource(benchmarkState.getBatchSize()));

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void nextMessage(Benchmark request, StreamObserver<Batch> responseObserver) {
        Logger.debug("nextMessage");

        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        Batch batch = this.benchmark.get(request.getId()).getNextBatch();
        responseObserver.onNext(batch);
        responseObserver.onCompleted();
    }

    @Override
    public void processed(Result request, StreamObserver<Empty> responseObserver) {
        Logger.debug("processed");

        long nanoTime = System.nanoTime();

        if(!this.benchmark.containsKey(request.getBenchmarkId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        this.benchmark.get(request.getBenchmarkId()).processed(request, nanoTime);

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void endBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        Logger.debug("endBenchmark");
        long nanoTime = System.nanoTime();

        if(!this.benchmark.containsKey(request.getId())) {
            responseObserver.onError(new Exception("Benchmark not started"));
            return;
        }

        this.benchmark.get(request.getId()).endBenchmark(nanoTime);



        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
