package de.tum.i13;

import de.tum.i13.bandency.ChallengerGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.tinylog.Logger;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        try {

            Logger.info("Challenger Service");

            PrepareLocationDataset pld = new PrepareLocationDataset(Path.of("/home/chris/data/challenge"));
            Logger.info("Loading Locationdata");
            LocationDataset ld = pld.loadData();


            Logger.info("Initializing Challenger Service");
            ChallengerServer cs = new ChallengerServer(ld);


            Logger.info("Initializing Service");
            Server server = ServerBuilder.forPort(8081).addService(cs).build();
            server.start();
            Logger.info("Serving");
            server.awaitTermination();
        } catch (Exception ex) {
            Logger.error(ex);
        }

        return;
    }
}
