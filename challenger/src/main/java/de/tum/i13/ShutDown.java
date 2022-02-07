package de.tum.i13;

import de.tum.i13.dal.ResultsVerifier;
import io.grpc.Server;
import org.tinylog.Logger;

import com.zaxxer.hikari.HikariDataSource;

public class ShutDown extends Thread {
    private final ResultsVerifier rv;
    private final Server server;
    private final HikariDataSource db;

    public ShutDown(ResultsVerifier rv, Server server, HikariDataSource db) {
        this.rv = rv;
        this.server = server;
        this.db = db;
    }

    @Override
    public void run() {
        this.server.shutdown();
        Logger.info("Server shutdown");
        rv.shutdown();
        Logger.info("ResultsVerifier shutdown");
        db.close();
        Logger.info("Disconnect DB");
    }
}
