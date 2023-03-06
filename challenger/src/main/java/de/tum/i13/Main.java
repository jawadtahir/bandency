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
            File datasetDirectory = new File("/home/chris/source/bandency/web/fetchdata");
            String url = "jdbc:postgresql://winhost:5432/bandency?user=bandency&password=bandency";
            int durationEvaluationMinutes = 1;

            // Override default values
            if(hostName.equalsIgnoreCase("node-22") || hostName.equalsIgnoreCase("node-11")) {
                datasetDirectory = new File(env.get("DATASET_DIRECTORY"));
                url = env.get("JDBC_DB_CONNECTION");
                durationEvaluationMinutes = 15;
            }

            ArrayList<File> datasetFiles = Utils.getFiles(datasetDirectory);
            datasetFiles.stream().forEach(f -> Logger.info("Using the following datasets: " + f.getName()));

            var bl = new BatchedCollector(100);

            var hl = new HddLoader(bl, datasetFiles.get(1));
            hl.load();






            
            //Test Dataset
            // StringZipFile szfTest = new StringZipFile(Path.of(datasetTest).toFile());
            //StringZipFileIterator szfiTest = szfTest.open();
            // FinancialEventLoader fdlTest = new FinancialEventLoader(szfiTest);
            // BatchedEvents beTest = new BatchedEvents(sg);
            //Logger.info("Preloading data in memory - Test: " + datasetTest);
            // beTest.loadData(fdlTest, 1_000);
            // Logger.info("Test Count - " + beTest.batchCount());


            // BatchedEvents beEvaluation = beTest;
            if(hostName.equalsIgnoreCase("node-22") || hostName.equalsIgnoreCase("node-11")) {
                //Evaluation Dataset
                //StringZipFile szfEvaluation = new StringZipFile(Path.of(datasetEvaluation).toFile());
                //StringZipFileIterator szfiEvaluation = szfEvaluation.open();
                // FinancialEventLoader fdlEvaluation = new FinancialEventLoader(szfiEvaluation);
                // beEvaluation = new BatchedEvents(sg);
              //  Logger.info("Preloading data in memory - Evaluation: " + datasetEvaluation);
                // beEvaluation.loadData(fdlEvaluation, 10_000);
                // Logger.info("Evaluation Count - " + beEvaluation.batchCount());
            } else {
                Logger.info("Using test set also for evaluation");
            }
            
            
            Logger.info("Evaluation duration in minutes: " + durationEvaluationMinutes);
            
            ArrayBlockingQueue<ToVerify> verificationQueue = new ArrayBlockingQueue<>(1_000_000, false);

            Logger.info("Initializing Challenger Service");
            Logger.info("opening database connection: " + url);
            var connectionPool = new DB(url);
            var connection = connectionPool.getConnection();
            Queries q = new Queries(connectionPool);
            // ChallengerServer cs = new ChallengerServer(beTest, beEvaluation, verificationQueue, q, durationEvaluationMinutes);

            Logger.info("Initializing Service");
            Server server = ServerBuilder
                    .forPort(8081)
            //        .addService(cs)
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
