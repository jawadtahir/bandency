package org.debs.gc2023;

import io.grpc.Server;

import java.sql.SQLException;
import java.util.ArrayList;

import org.debs.gc2023.dal.IQueries;
import org.debs.gc2023.dal.ResultsVerifier;
import org.tinylog.Logger;

public class ShutDown extends Thread {
    private final ResultsVerifier rv;
    private final Server server;
    private final IQueries q;
    private ArrayList<AutoCloseable> toclose;

    public ShutDown(ResultsVerifier rv, Server server, IQueries q, ArrayList<AutoCloseable> toclose) {
        this.rv = rv;
        this.server = server;
        this.q = q;
        this.toclose = toclose;
    }

    @Override
    public void run() {
        this.server.shutdown();
        Logger.info("Server shutdown");
        rv.shutdown();
        Logger.info("ResultsVerifier shutdown");
        try {
            if(q != null) {
                if(q.getDb() != null) {
                    q.getDb().getConnection().close();
                }
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Logger.info("Disconnect DB");

        for (AutoCloseable autoCloseable : toclose) {
            try {
                autoCloseable.close();
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
