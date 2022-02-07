package de.tum.i13.dal;

import com.zaxxer.hikari.HikariDataSource;

public class DB {
    public static HikariDataSource getConnectionPool(String url) throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setMaximumPoolSize(10);
        ds.setKeepaliveTime(30_001);

        return ds;        
    }
}
