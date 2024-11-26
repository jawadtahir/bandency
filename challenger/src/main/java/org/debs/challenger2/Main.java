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
import java.util.concurrent.ArrayBlockingQueue;


public class Main {

    static RestServer restServer = null;
    private static final String DATA_DIR = "/home/foobar/PhD/Data/DEBS/imaging/archive/L-PBF Dataset/Build 1/Test";

    private static Logger logger = LogManager.getLogger(Main.class);
//    public static final String DB_CONNECTION = "mongodb://myDatabaseUser:D1fficultP%40ssw0rd@mongodb0.example.com:52925/";
public static final String DB_CONNECTION = "mongodb://localhost:52925/";
    public static final String DATABASE = "challenger";
    public static void main(String[] args) throws IOException {
        IDataStore store = new InMemoryDataStore();
        logger.info("Reading data files");
        DataLoader dataLoader = new DataLoader(store, DATA_DIR);
        dataLoader.load();
        ArrayBlockingQueue<IPendingTask> dbInserter = new ArrayBlockingQueue<>(1_000_000, false);
        IQueries q = new MongoQueries(DB_CONNECTION, DATABASE);
        int evalDuration = 10;

        restServer = new RestServer(store, dbInserter, q, evalDuration);

        JAXRSServerFactoryBean serverFactoryBean = new JAXRSServerFactoryBean();
        serverFactoryBean.setResourceClasses(RestServer.class);
        serverFactoryBean.setServiceBean(restServer);
        //serverFactoryBean.setResourceProvider(restServer.getClass(), new SingletonResourceProvider(restServer.getClass()));
        serverFactoryBean.setAddress("http://localhost:52923/");
        serverFactoryBean.create();

        logger.info("Started REST Server");

        PendingTaskRunner taskRunner = new PendingTaskRunner(dbInserter, q);
        taskRunner.run();

        logger.info("Started pending task executor");

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(serverFactoryBean.getServer(), q, taskRunner)));
        logger.info("Added shutdown hook");





    }
}
