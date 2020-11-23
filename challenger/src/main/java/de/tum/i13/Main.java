package de.tum.i13;

import de.tum.i13.dal.ResultsVerifier;
import de.tum.i13.dal.ToVerify;
import de.tum.i13.datasets.airquality.*;
import de.tum.i13.datasets.location.LocationDataset;
import de.tum.i13.datasets.location.PrepareLocationDataset;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.tinylog.Logger;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {

    public static void main(String[] args) {

        try {

            String dataset = "/home/chris/data/challenge";
            String hostName = InetAddress.getLocalHost().getHostName();

            if(hostName.equalsIgnoreCase("node-22")) {
                dataset = "/home/msrg/data/luftdaten";
            }

            Logger.info("Challenger Service: hostname: " + hostName + " datasetsfolder: " + dataset);
            PrepareLocationDataset pld = new PrepareLocationDataset(Path.of(dataset));
            LocationDataset ld = pld.loadData();

            AirqualityFileAccess afa = new AirqualityFileAccess(Path.of(dataset));
            AirqualityDataset ad = new AirqualityDataset(afa, AccessType.FromDisk);

            ArrayBlockingQueue<ToVerify> verificationQueue = new ArrayBlockingQueue<ToVerify>(1_000_000, false);
            ResultsVerifier rv = new ResultsVerifier(verificationQueue);

            Logger.info("Initializing Challenger Service");
            ChallengerServer cs = new ChallengerServer(ld, ad, verificationQueue);


            Logger.info("Initializing Service");
            Server server = ServerBuilder
                    .forPort(8081)
                    .addService(cs)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .build();

            server.start();
            Logger.info("Starting Results verifier");
            Thread th = new Thread(rv);
            th.start();


            Runtime current = Runtime.getRuntime();
            current.addShutdownHook(new ShutDown(rv, server));

            Logger.info("Serving");
            server.awaitTermination();

        } catch (Exception ex) {
            Logger.error(ex);
        }

        return;
    }
}
