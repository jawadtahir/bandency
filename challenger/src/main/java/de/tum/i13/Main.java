package de.tum.i13;

import de.tum.i13.dal.DB;
import de.tum.i13.dal.Queries;
import de.tum.i13.dal.ResultsVerifier;
import de.tum.i13.dal.ToVerify;
import de.tum.i13.datasets.hdd.BatchedCollector;
import de.tum.i13.datasets.hdd.HddLoader;
import de.tum.i13.datasets.util.Utils;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.prometheus.client.exporter.HTTPServer;
import org.tinylog.Logger;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {

    public static void main(String[] args) {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            Map<String, String> env = System.getenv();

            // Default values
            File datasetDirectory = new File(env.get("HOME") + "/source/bandency/web/fetchdata");
            String url = "jdbc:postgresql://localhost:5432/bandency?user=bandency&password=bandency";
            int durationEvaluationMinutes = 1;
            int maxBatches = 100;

            // Override default values on the big machine with slightly more ram than my laptop
            if(hostName.equalsIgnoreCase("cervino-1")) {
                datasetDirectory = new File(env.get("DATASET_DIRECTORY"));
                url = env.get("JDBC_DB_CONNECTION");
                durationEvaluationMinutes = 15;
                maxBatches = 100_000;
            }

            Logger.info("Initializing Challenger Service");
            Logger.info("opening database connection: " + url);
            var connectionPool = new DB(url);

            // We do this here to test the DB connection
            var connection = connectionPool.getConnection();
            Queries q = new Queries(connectionPool);


            ArrayList<File> datasetFiles = Utils.getFiles(datasetDirectory);
            datasetFiles.stream().forEach(f -> Logger.info("Using the following datasets: " + f.getName()));

            var bl = new BatchedCollector(1000, maxBatches);

            Logger.info("Preloading data in memory");
            if(hostName.equalsIgnoreCase("cervino-1")) {
                //Load the full dataset
                for (File f : datasetFiles) {
                    var hl = new HddLoader(bl, f); // -1 means load all
                    if(!hl.load()) {
                        break;
                    }
                }
            } else {
                var hl = new HddLoader(bl, datasetFiles.get(0));
                hl.load();
            }

            Logger.info("Loaded " + bl.batchCount() + " batches");            
            
            Logger.info("Evaluation duration in minutes: " + durationEvaluationMinutes);
            
            ArrayBlockingQueue<ToVerify> verificationQueue = new ArrayBlockingQueue<>(1_000_000, false);

            // To kick it off, currently TEST and Evaluation are the same
            ChallengerServer cs = new ChallengerServer(bl, bl, verificationQueue, q, durationEvaluationMinutes);

            Logger.info("Initializing Service");
            Server server = ServerBuilder
                    .forPort(5023)
                    .addService(cs)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .build();

            server.start();

            Logger.info("Initilize Prometheus");
            var metrics = new HTTPServer(8023); //This starts already a background thread serving the default registry

            Logger.info("Starting Results verifier");
            ResultsVerifier rv = new ResultsVerifier(verificationQueue, q);
            Thread th = new Thread(rv);
            th.start();


            Runtime current = Runtime.getRuntime();
            current.addShutdownHook(new ShutDown(rv, server, connectionPool));

            Logger.info("Serving");
            server.awaitTermination();
            metrics.close();

        } catch (Exception ex) {
            Logger.error(ex);
        }

        return;
    }
}
