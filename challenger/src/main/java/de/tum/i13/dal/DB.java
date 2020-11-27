package de.tum.i13.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private Connection connection;
    private DB(Connection connection) throws SQLException {
        this.connection = connection;
    }

    public static DB createDBConnection(String url) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection(url);

        return new DB(connection);
    }

    public Connection getConnection() {
        return connection;
    }
}
