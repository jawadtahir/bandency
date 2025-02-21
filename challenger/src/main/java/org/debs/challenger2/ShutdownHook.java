package org.debs.challenger2;

import org.apache.cxf.endpoint.Server;
import org.debs.challenger2.db.IQueries;
import org.debs.challenger2.pending.PendingTaskRunner;

import java.io.IOException;

public class ShutdownHook implements Runnable{

    private final Server restServer;
    private final IQueries queryImpl;
    private final PendingTaskRunner taskRunner;

    public ShutdownHook(Server restServer, IQueries queries, PendingTaskRunner taskRunner){
        this.restServer = restServer;
        this.queryImpl = queries;
        this.taskRunner = taskRunner;
    }
    @Override
    public void run() {

        restServer.destroy();

        try {
            taskRunner.close();
            queryImpl.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
