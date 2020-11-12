package de.tum.i13;

import com.google.protobuf.Empty;
import de.tum.i13.bandency.*;
import de.tum.i13.datasets.location.LocationDataset;
import io.grpc.stub.StreamObserver;

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
        super.createNewBenchmark(request, responseObserver);
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
