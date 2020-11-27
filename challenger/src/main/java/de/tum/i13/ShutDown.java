package de.tum.i13;

import de.tum.i13.dal.DB;
import de.tum.i13.dal.ResultsVerifier;
import io.grpc.Server;
import org.tinylog.Logger;

import java.sql.SQLException;

public class ShutDown extends Thread {
    private final ResultsVerifier rv;
    private final Server server;
    private final DB db;

    public ShutDown(ResultsVerifier rv, Server server, DB db) {
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
        try {
            db.getConnection().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        Logger.info("Disconnect DB");
    }
}
