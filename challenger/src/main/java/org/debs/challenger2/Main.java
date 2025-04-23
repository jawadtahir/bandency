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
    public static final String DB_CONNECTION = System.getenv().getOrDefault("DB_CONNECTION", "mongodb://localhost:52926/");
    public static final String REST_PORT = System.getenv().getOrDefault("REST_PORT", "52923");
    public static final String DATABASE = "challenger";
    public static void main(String[] args) throws IOException {
        String dataDir = System.getenv().getOrDefault("DATA_DIR", "/data");
        String dataDirEval = System.getenv().getOrDefault("DATA_DIR_EVAL", null);

        logger.info("The data directory is {}",dataDir);
        logger.info("The evaluation data directory is {}",dataDirEval);
        logger.info("Database connection string is {}", DB_CONNECTION);
        logger.info("The REST server will listen to port {}", REST_PORT);

        IDataStore testStore = loadData(dataDir);
        IDataStore evalStore = loadData(dataDirEval);

        ArrayBlockingQueue<IPendingTask> pendingTasks = new ArrayBlockingQueue<>(1_000_000, false);
        IQueries q = new MongoQueries(DB_CONNECTION, DATABASE);

        restServer = new RestServer(testStore, evalStore, pendingTasks, q);

        String restAddress = String.format("http://0.0.0.0:%s/", REST_PORT);

        JAXRSServerFactoryBean serverFactoryBean = new JAXRSServerFactoryBean();
//        serverFactoryBean.setResourceClasses(RestServer.class);
        serverFactoryBean.setServiceBean(restServer);
        //serverFactoryBean.setResourceProvider(restServer.getClass(), new SingletonResourceProvider(restServer.getClass()));
        serverFactoryBean.setAddress(restAddress);
        serverFactoryBean.create();

        logger.info("Started REST Server");

        PendingTaskRunner taskRunner = new PendingTaskRunner(pendingTasks, q);
        taskRunner.run();

        logger.info("Started pending task executor");

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(serverFactoryBean.getServer(), q, taskRunner)));
        logger.info("Added shutdown hook");

    }

    public static IDataStore loadData(String dataDir){
        IDataStore store = new InMemoryDataStore();

        Path dirPath = Paths.get(dataDir);
        if (Files.notExists(dirPath)){
            logger.warn("No such directory. %s%n", dataDir);
            return null;
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)){
            if (!dirStream.iterator().hasNext()){
                System.out.println("Found no files");
                return null;
            }
            logger.info("Reading data files");
            DataLoader dataLoader = new DataLoader(store, dataDir);
            dataLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return store;

    }
}
