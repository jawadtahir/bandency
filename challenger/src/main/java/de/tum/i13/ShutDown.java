package de.tum.i13;

import de.tum.i13.dal.ResultsVerifier;
import io.grpc.Server;
import org.tinylog.Logger;

public class ShutDown extends Thread {
    private final ResultsVerifier rv;
    private final Server server;

    public ShutDown(ResultsVerifier rv, Server server) {
        this.rv = rv;
        this.server = server;
    }

    @Override
    public void run() {
        this.server.shutdown();
        Logger.info("Server shutdown");
        rv.shutdown();
        Logger.info("ResultsVerifier shutdown");
    }
}
