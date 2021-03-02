package de.tum.i13;

import de.tum.i13.dal.DB;
import de.tum.i13.dal.Queries;
import de.tum.i13.dal.ResultsVerifier;
import de.tum.i13.dal.ToVerify;
import de.tum.i13.datasets.airquality.AccessType;
import de.tum.i13.datasets.airquality.AirqualityDataset;
import de.tum.i13.datasets.airquality.AirqualityFileAccess;
import de.tum.i13.datasets.location.PrepareLocationDataset;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.prometheus.client.exporter.HTTPServer;
import org.tinylog.Logger;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {

    public static void main(String[] args) {
        try {

            Map<String, String> env = System.getenv();

            String dataset = "/home/chris/data/challenge";
            String hostName = InetAddress.getLocalHost().getHostName();

            String url = "jdbc:postgresql://127.0.0.1:5432/bandency?user=bandency&password=bandency";

            if(hostName.equalsIgnoreCase("node-22") || hostName.equalsIgnoreCase("node-11")) {
                dataset = "/home/msrg/data/luftdaten";
                url = env.get("JDBC_DB_CONNECTION");
            }

            Logger.info("opening database connection: " + url);
            DB db = DB.createDBConnection(url);

            Logger.info("Challenger Service: hostname: " + hostName + " datasetsfolder: " + dataset);
            PrepareLocationDataset pld = new PrepareLocationDataset(Path.of(dataset));

            AirqualityFileAccess afa = new AirqualityFileAccess(Path.of(dataset));
            AirqualityDataset ad = new AirqualityDataset(afa, AccessType.FromDisk);

            ArrayBlockingQueue<ToVerify> verificationQueue = new ArrayBlockingQueue<>(1_000_000, false);

            Logger.info("Initializing Challenger Service");
            Queries q = new Queries(db.getConnection());
            ChallengerServer cs = new ChallengerServer(pld, ad, verificationQueue, q);

            Logger.info("Initializing Service");
            Server server = ServerBuilder
                    .forPort(8081)
                    .addService(cs)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .build();

            server.start();

            Logger.info("Initilize Prometheus");
            new HTTPServer(8023); //This starts already a background thread serving the default registry

            Logger.info("Starting Results verifier");
            ResultsVerifier rv = new ResultsVerifier(verificationQueue, db.getConnection());
            Thread th = new Thread(rv);
            th.start();


            Runtime current = Runtime.getRuntime();
            current.addShutdownHook(new ShutDown(rv, server, db));

            Logger.info("Serving");
            server.awaitTermination();

        } catch (Exception ex) {
            Logger.error(ex);
        }

        return;
    }
}
