package de.tum.i13;

import com.google.protobuf.Empty;
import de.tum.i13.bandency.*;
import de.tum.i13.datasets.location.LocationDataset;
import io.grpc.stub.StreamObserver;

import java.util.Random;

public class ChallengerServer extends ChallengerGrpc.ChallengerImplBase {
    final private LocationDataset ld;

    public ChallengerServer(LocationDataset ld) {
        this.ld = ld;
    }

    @Override
    public void getLocations(Empty request, StreamObserver<Locations> responseObserver) {
        responseObserver.onNext(ld.getAllLocations());
        responseObserver.onCompleted();
    }

    @Override
    public void createNewBenchmark(BenchmarkConfiguration request, StreamObserver<Benchmark> responseObserver) {

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

        Benchmark bm = Benchmark.newBuilder()
                .setId(random_id)
                .build();

        responseObserver.onNext(bm);
        responseObserver.onCompleted();
    }

    @Override
    public void initializeLatencyMeasuring(Benchmark request, StreamObserver<Ping> responseObserver) {
        super.initializeLatencyMeasuring(request, responseObserver);
    }

    @Override
    public void measure(Ping request, StreamObserver<Ping> responseObserver) {
        super.measure(request, responseObserver);
    }

    @Override
    public void endMeasurement(Ping request, StreamObserver<Empty> responseObserver) {
        super.endMeasurement(request, responseObserver);
    }

    @Override
    public void startBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        super.startBenchmark(request, responseObserver);
    }

    @Override
    public void nextMessage(Benchmark request, StreamObserver<Payload> responseObserver) {
        super.nextMessage(request, responseObserver);
    }

    @Override
    public void processed(Result request, StreamObserver<Empty> responseObserver) {
        super.processed(request, responseObserver);
    }

    @Override
    public void endBenchmark(Benchmark request, StreamObserver<Empty> responseObserver) {
        super.endBenchmark(request, responseObserver);
    }
}
