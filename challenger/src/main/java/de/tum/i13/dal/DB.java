package de.tum.i13.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.tinylog.Logger;

public class DB {
    private Connection connection;
    private String url;

    private final Object lock = new Object();


    public DB(String url) throws SQLException, ClassNotFoundException {
        this.url = url;
        this.connection = newConnection();
    }

    private Connection newConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(url);
    }

    public Connection getConnection() throws InterruptedException, ClassNotFoundException {
        try { //happy path, return the connection if valid
            if(!this.connection.isClosed()) {
                return this.connection;
            }
        } catch (SQLException ex) {
            synchronized(this.lock) {
                //Double reentrant should return immediately
                try {
                    if(!this.connection.isClosed()) {
                        return this.connection;
                    }    
                } catch(SQLException innerEx) {
                    //Lets try 30 times, first wait 1 second, next 2 seconds, ...
                    for(int i = 1; i < 31; ++i) {
                        try {
                            this.connection = newConnection();
                            if(!this.connection.isClosed()) {
                                return this.connection;
                            }
                        } catch(SQLException innerInnerEx) {
                            Logger.info("Failed attempt to connect, cnt: " + i + " exception: " + ex);
                        }
                        int millisecondsWait = 1000 * i;
                        System.out.println("Retrying ... " + millisecondsWait);
                        Thread.sleep(millisecondsWait);
                    }
                    Logger.error("failed to create a valid connection");    
                }    
            }
        }

        return connection;
    }
}
