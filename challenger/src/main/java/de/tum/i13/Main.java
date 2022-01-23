package de.tum.i13;

import de.tum.i13.dal.DB;
import de.tum.i13.dal.Queries;
import de.tum.i13.dal.ResultsVerifier;
import de.tum.i13.dal.ToVerify;
import de.tum.i13.datasets.airquality.StringZipFile;
import de.tum.i13.datasets.airquality.StringZipFileIterator;
import de.tum.i13.datasets.financial.BatchedEvents;
import de.tum.i13.datasets.financial.FinancialEventLoader;
import de.tum.i13.datasets.financial.SymbolsGenerator;
import de.tum.i13.datasets.financial.SymbolsReader;
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

            String datasetTest = "/home/chris/data/debs-gc-2022-test-data.zip";
            String datasetEvaluation = "/home/chris/data/trading-week-onecsv-purged.zip";

            String symbolDataset = "/home/chris/data/symbols-unique.txt";
            String hostName = InetAddress.getLocalHost().getHostName();

            String url = "jdbc:postgresql://winhost:5432/bandency?user=bandency&password=bandency";
            int preloadEvaluation = 100;
            int durationEvaluationMinutes = 1;

            if(hostName.equalsIgnoreCase("node-22") || hostName.equalsIgnoreCase("node-11")) {
                datasetTest = env.get("DATASET_PATH_TEST");
                datasetEvaluation = env.get("DATASET_PATH_EVALUATION");
                symbolDataset = env.get("SYMBOL_DATASET");
                url = env.get("JDBC_DB_CONNECTION");
                preloadEvaluation = 30_000;
                durationEvaluationMinutes = 15;
            }

            Logger.info("Challenger Service: hostname: " + hostName + " datasetsfolder: " + datasetTest);

            SymbolsReader sr = new SymbolsReader(symbolDataset);
            var symbols = sr.readAll();
            symbols.sort((l, r) -> Integer.compare(r.getOccurances(), l.getOccurances()));

            var sg = new SymbolsGenerator(symbols);
            
            StringZipFile szf = new StringZipFile(Path.of(datasetTest).toFile());
            StringZipFileIterator szfi = szf.open();
            FinancialEventLoader fdl = new FinancialEventLoader(szfi);

            BatchedEvents beEvaluation = new BatchedEvents(sg);
            Logger.info("Preloading data in memory: " + preloadEvaluation);
            beEvaluation.loadData(fdl, 1000);

            Logger.info("Test Count - " + beEvaluation.batchCount());
            
            Logger.info("Evaluation duration in minutes: " + durationEvaluationMinutes);
            
            ArrayBlockingQueue<ToVerify> verificationQueue = new ArrayBlockingQueue<>(1_000_000, false);

            Logger.info("Initializing Challenger Service");
            Logger.info("opening database connection: " + url);
            DB db = DB.createDBConnection(url);
            Queries q = new Queries(db.getConnection());
            ChallengerServer cs = new ChallengerServer(beEvaluation, verificationQueue, q, durationEvaluationMinutes);

            Logger.info("Initializing Service");
            Server server = ServerBuilder
                    .forPort(8081)
                    .addService(cs)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .build();

            server.start();

            Logger.info("Initilize Prometheus");
            var metrics = new HTTPServer(8023); //This starts already a background thread serving the default registry

            Logger.info("Starting Results verifier");
            ResultsVerifier rv = new ResultsVerifier(verificationQueue, db.getConnection());
            Thread th = new Thread(rv);
            th.start();


            Runtime current = Runtime.getRuntime();
            current.addShutdownHook(new ShutDown(rv, server, db));

            Logger.info("Serving");
            server.awaitTermination();
            metrics.close();

        } catch (Exception ex) {
            Logger.error(ex);
        }

        return;
    }
}
