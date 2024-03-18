package org.debs.gc2023;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.prometheus.client.exporter.HTTPServer;
import org.debs.gc2023.dal.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.prometheus.client.exporter.HTTPServer;

import org.debs.gc2023.dal.DB;
import org.debs.gc2023.dal.IQueries;
import org.debs.gc2023.dal.NoopQueries;
import org.debs.gc2023.dal.Queries;
import org.debs.gc2023.dal.ResultsVerifier;
import org.debs.gc2023.dal.ToVerify;
import org.debs.gc2023.datasets.IDataStore;
import org.debs.gc2023.datasets.disc.RocksdbStore;
import org.debs.gc2023.datasets.inmemory.BatchedCollector;
import org.debs.gc2023.datasets.inmemory.HddLoader;
import org.debs.gc2023.datasets.inmemory.InMemoryDataStore;
import org.debs.gc2023.datasets.util.Utils;
import org.rocksdb.FlushOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        ArrayList<AutoCloseable> toclose = new ArrayList<AutoCloseable>();
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            Map<String, String> env = System.getenv();

            // Default values
            File datasetDirectory = new File(env.get("HOME") + "/source/bandency/web/fetchdata");
            String url =
                    "jdbc:postgresql://172.24.33.107:5432/bandency?user=bandency&password=bandency-high-5";
            int durationEvaluationMinutes = 15;
            int maxBatches = 191;
            Boolean inMemoryDataset = true;
            Boolean useDatabase = true;

            // Override default values on the big machine with slightly more ram than my laptop
            if (hostName.equalsIgnoreCase("L3-37")) {
                datasetDirectory = new File(env.get("HOME") + "/source/bandency/web/fetchdata");
                url = "jdbc:postgresql://172.22.80.1:5432/bandency?user=bandency&password=bandency";
                durationEvaluationMinutes = 15;
                maxBatches = 100_000;
                inMemoryDataset = false;
            } else if (hostName.equalsIgnoreCase("debs-2023")) {
                datasetDirectory = new File(env.get("DATASET_DIRECTORY"));
                url = env.get("JDBC_DB_CONNECTION");
                durationEvaluationMinutes = 15;
                maxBatches = 100_000;
                inMemoryDataset = false;
                useDatabase = true;
            } else if (hostName.equalsIgnoreCase("disvm4")) {
                url = env.get("JDBC_DB_CONNECTION");
                durationEvaluationMinutes = 15;
                maxBatches = 100_000;
                inMemoryDataset = false;
                useDatabase = true;
            } else if (hostName.equalsIgnoreCase("2-1B")) {
                url = "";
                durationEvaluationMinutes = 15;
                maxBatches = 100_000;
                inMemoryDataset = false;
                useDatabase = false;
            } else {
                if (env.containsKey("DATASET_DIRECTORY")) {
                    datasetDirectory = new File(env.get("DATASET_DIRECTORY"));
                }
                if (env.containsKey("JDBC_DB_CONNECTION")) {
                    url = env.get("JDBC_DB_CONNECTION");
                } else {
                    useDatabase = false;
                }

                if (env.containsKey("INMEMORY_DATASET")) {
                    inMemoryDataset = Boolean.parseBoolean(env.get("INMEMORY_DATASET"));
                }
            }

            Logger.info("Initializing Challenger Service");

            IQueries q = null;
            if (useDatabase) {
                Logger.info("opening database connection: " + url);
                var connectionPool = new DB(url);
                // We do this here to test the DB connection
                connectionPool.getConnection();
                q = new Queries(connectionPool);
            } else {
                q = new NoopQueries();
            }

            ArrayList<File> datasetFiles = Utils.getFiles(datasetDirectory);
            datasetFiles.stream()
                    .forEach(f -> Logger.info("Using the following datasets: " + f.getName()));

            IDataStore store = null;

            if (inMemoryDataset) {
                store = new InMemoryDataStore();
                var bl = new BatchedCollector(store, 1000, maxBatches);

                Logger.info("Preloading data in memory");
                // Load the full dataset
                for (File f : datasetFiles) {
                    var hl = new HddLoader(bl, f); // -1 means load all
                    if (!hl.load()) {
                        break;
                    }
                }
                Logger.info("Loaded " + bl.batchCount() + " batches");
            } else {
                RocksDB.loadLibrary();
                var dbPath = "local.db";

                File dbFile = new File(dbPath);
                var needsToBeInitialized = !dbFile.exists();

                Options options = new Options().setCreateIfMissing(true);
                RocksDB db = RocksDB.open(options, dbPath);
                toclose.add(db);
                toclose.add(options);
                store = new RocksdbStore(db);

                if (needsToBeInitialized) {
                    var bl = new BatchedCollector(store, 1000, maxBatches);
                    Logger.info("Filling database");
                    // Load the full dataset
                    for (final File f : datasetFiles) {
                        var hl = new HddLoader(bl, f); // -1 means load all
                        if (!hl.load()) {
                            break;
                        }
                    }
                    Logger.info("Loaded " + bl.batchCount() + " batches");
                }
                var flushoptions = new FlushOptions().setWaitForFlush(true);
                toclose.add(flushoptions);
                db.flush(flushoptions);
            }


            Logger.info("Evaluation duration in minutes: " + durationEvaluationMinutes);

            ArrayBlockingQueue<ToVerify> verificationQueue =
                    new ArrayBlockingQueue<>(1_000_000, false);

            // To kick it off, currently TEST and Evaluation are the same
            ChallengerServer cs =
                    new ChallengerServer(store, verificationQueue, q, durationEvaluationMinutes);
            Logger.info("Initializing Service");
            Server server = ServerBuilder.forPort(5023).addService(cs)
                    .maxInboundMessageSize(10 * 1024 * 1024).build();
            server.start();

            Logger.info("Initilize Prometheus");
            var metrics = new HTTPServer(8023); // This starts already a background thread serving
                                                // the default registry

            Logger.info("Starting Results verifier");
            ResultsVerifier rv = new ResultsVerifier(verificationQueue, q);
            Thread th = new Thread(rv);
            th.start();
            server.awaitTermination();
            metrics.close();

        } catch (Exception ex) {
            Logger.error(ex);
            for (AutoCloseable autoCloseable : toclose) {
                try {
                    Logger.info("closing in  the try catch close...");
                    autoCloseable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return;
    }
}
