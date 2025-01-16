package org.debs.challenger2;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.debs.challenger2.dataset.DataLoader;
import org.debs.challenger2.dataset.IDataStore;
import org.debs.challenger2.dataset.InMemoryDataStore;
import org.debs.challenger2.db.IQueries;
import org.debs.challenger2.db.MongoQueries;
import org.debs.challenger2.pending.IPendingTask;
import org.debs.challenger2.pending.PendingTaskRunner;
import org.debs.challenger2.rest.RestServer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;


public class Main {

    static RestServer restServer = null;

    private static Logger logger = LogManager.getLogger(Main.class);
//    public static final String DB_CONNECTION = "mongodb://myDatabaseUser:D1fficultP%40ssw0rd@mongodb0.example.com:52925/";
    public static final String DB_CONNECTION = System.getProperty("DB_CONNECTION", "mongodb://localhost:52925/");
    public static final String REST_PORT = System.getProperty("REST_PORT", "52923");
    public static final String DATABASE = "challenger";
    public static void main(String[] args) throws IOException {
        String dataDir = System.getenv().getOrDefault("DATA_DIR", "/data");
        Path dirPath = Paths.get(dataDir);
        System.out.println(dirPath.toAbsolutePath());
//        Files.list(dirPath).forEach(path -> System.out.println(path.toAbsolutePath()));
        if (Files.notExists(dirPath)){
            System.out.println("No such directory");
            return;
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)){
            if (!dirStream.iterator().hasNext()){
                System.out.println("Found no files");
                return;
            }
        }

        IDataStore store = new InMemoryDataStore();
        logger.info("Reading data files");
        DataLoader dataLoader = new DataLoader(store, dataDir);
        dataLoader.load();
        ArrayBlockingQueue<IPendingTask> dbInserter = new ArrayBlockingQueue<>(1_000_000, false);
        IQueries q = new MongoQueries(DB_CONNECTION, DATABASE);
        int evalDuration = 10;

        restServer = new RestServer(store, dbInserter, q, evalDuration);

        String restAddress = String.format("http://localhost:%s/", REST_PORT);

        JAXRSServerFactoryBean serverFactoryBean = new JAXRSServerFactoryBean();
        serverFactoryBean.setResourceClasses(RestServer.class);
        serverFactoryBean.setServiceBean(restServer);
        //serverFactoryBean.setResourceProvider(restServer.getClass(), new SingletonResourceProvider(restServer.getClass()));
        serverFactoryBean.setAddress(restAddress);
        serverFactoryBean.create();

        logger.info("Started REST Server");

        PendingTaskRunner taskRunner = new PendingTaskRunner(dbInserter, q);
        taskRunner.run();

        logger.info("Started pending task executor");

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(serverFactoryBean.getServer(), q, taskRunner)));
        logger.info("Added shutdown hook");





    }
}
