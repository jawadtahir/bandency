package org.debs.challenger2;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.debs.challenger2.benchmark.ToVerify;
import org.debs.challenger2.dataset.DataLoader;
import org.debs.challenger2.dataset.IDataStore;
import org.debs.challenger2.dataset.InMemoryDataStore;
import org.debs.challenger2.db.NoopQueries;
import org.debs.challenger2.rest.RestServer;
import org.debs.challenger2.db.IQueries;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;


public class Main {

    static RestServer restServer = null;
    private static final String DATA_DIR = "/home/foobar/PhD/Data/DEBS/imaging/archive/L-PBF Dataset/Build 1/Test";

    private static Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
        IDataStore store = new InMemoryDataStore();
        logger.info("Reading data files");
        DataLoader dataLoader = new DataLoader(store, DATA_DIR);
        dataLoader.load();
        ArrayBlockingQueue<ToVerify> dbInserter = null;
        IQueries q = new NoopQueries();
        int evalDuration = 10;

        restServer = new RestServer(store, dbInserter, q, evalDuration);

        JAXRSServerFactoryBean serverFactoryBean = new JAXRSServerFactoryBean();
        serverFactoryBean.setResourceClasses(RestServer.class);
        serverFactoryBean.setServiceBean(restServer);
        //serverFactoryBean.setResourceProvider(restServer.getClass(), new SingletonResourceProvider(restServer.getClass()));
        serverFactoryBean.setAddress("http://localhost:52923/");
        serverFactoryBean.create();



        logger.info("Started REST Server");

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(serverFactoryBean.getServer())));
        logger.info("Added shutdown hook");





    }
}
