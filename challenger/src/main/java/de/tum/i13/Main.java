package de.tum.i13;

import de.tum.i13.bandency.Batch;
import de.tum.i13.dal.DB;
import de.tum.i13.dal.Queries;
import de.tum.i13.dal.ResultsVerifier;
import de.tum.i13.dal.ToVerify;
import de.tum.i13.datasets.airquality.StringZipFile;
import de.tum.i13.datasets.airquality.StringZipFileIterator;
import de.tum.i13.datasets.cache.InMemoryDataset;
import de.tum.i13.datasets.cache.InMemoryLoader;
import de.tum.i13.datasets.financial.FinancialDataLoader;
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
            int preloadEvaluation = 100;
            int durationEvaluationMinutes = 1;

            if(hostName.equalsIgnoreCase("node-22") || hostName.equalsIgnoreCase("node-11")) {
                dataset = env.get("DATASET_PATH");
                url = env.get("JDBC_DB_CONNECTION");
                preloadEvaluation = 30_000;
                durationEvaluationMinutes = 15;
            }

            Logger.info("opening database connection: " + url);
            DB db = DB.createDBConnection(url);

            Logger.info("Challenger Service: hostname: " + hostName + " datasetsfolder: " + dataset);
            

            StringZipFile szf = new StringZipFile(Path.of(dataset).toFile());
            StringZipFileIterator szfi = szf.open();
            FinancialDataLoader fdl = new FinancialDataLoader(szfi);
            InMemoryLoader<Batch> iml = new InMemoryLoader<Batch>(fdl);

            Logger.info("Preloading data in memory: " + preloadEvaluation);
            Logger.info("Evaluation duration in minutes: " + durationEvaluationMinutes);
            InMemoryDataset<Batch> inMemoryData = iml.loadData(1);

            ArrayBlockingQueue<ToVerify> verificationQueue = new ArrayBlockingQueue<>(1_000_000, false);

            Logger.info("Initializing Challenger Service");
            Queries q = new Queries(db.getConnection());
            ChallengerServer cs = new ChallengerServer(inMemoryData, verificationQueue, q, durationEvaluationMinutes);

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
