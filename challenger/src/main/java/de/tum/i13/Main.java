package de.tum.i13;

import de.tum.i13.dal.DB;
import de.tum.i13.dal.Queries;
import de.tum.i13.dal.ResultsVerifier;
import de.tum.i13.dal.ToVerify;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.prometheus.client.exporter.HTTPServer;
import org.tinylog.Logger;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {

    public static void main(String[] args) {
        try {
            Map<String, String> env = System.getenv();

            String datasetTest = "/home/chris/data/debs-gc-2022-test-data.zip";
            String datasetEvaluation = "/home/chris/data/trading-week-onecsv-purged.zip";

            String symbolDataset = "/home/chris/data/symbols-unique.txt";
            String hostName = InetAddress.getLocalHost().getHostName();

            String url = "jdbc:postgresql://winhost:5432/bandency?user=bandency&password=bandency";
            int durationEvaluationMinutes = 1;

            if(hostName.equalsIgnoreCase("node-22") || hostName.equalsIgnoreCase("node-11")) {
                datasetTest = env.get("DATASET_PATH_TEST");
                datasetEvaluation = env.get("DATASET_PATH_EVALUATION");
                symbolDataset = env.get("SYMBOL_DATASET");
                url = env.get("JDBC_DB_CONNECTION");
                durationEvaluationMinutes = 15;
            }

            Logger.info("Challenger Service: hostname: " + hostName + " datasetsfolder: " + datasetTest);
            
            //Test Dataset
            // StringZipFile szfTest = new StringZipFile(Path.of(datasetTest).toFile());
            //StringZipFileIterator szfiTest = szfTest.open();
            // FinancialEventLoader fdlTest = new FinancialEventLoader(szfiTest);
            // BatchedEvents beTest = new BatchedEvents(sg);
            Logger.info("Preloading data in memory - Test: " + datasetTest);
            // beTest.loadData(fdlTest, 1_000);
            // Logger.info("Test Count - " + beTest.batchCount());


            // BatchedEvents beEvaluation = beTest;
            if(hostName.equalsIgnoreCase("node-22") || hostName.equalsIgnoreCase("node-11")) {
                //Evaluation Dataset
                //StringZipFile szfEvaluation = new StringZipFile(Path.of(datasetEvaluation).toFile());
                //StringZipFileIterator szfiEvaluation = szfEvaluation.open();
                // FinancialEventLoader fdlEvaluation = new FinancialEventLoader(szfiEvaluation);
                // beEvaluation = new BatchedEvents(sg);
                Logger.info("Preloading data in memory - Evaluation: " + datasetEvaluation);
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
