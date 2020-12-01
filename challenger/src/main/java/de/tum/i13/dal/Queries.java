package de.tum.i13.dal;

import java.sql.*;

public class Queries {
    private final Connection conn;

    public Queries(Connection conn) {

        this.conn = conn;
    }

    public boolean checkIfGroupExists(String token) throws SQLException {
        try(PreparedStatement preparedStatement = this.conn.prepareStatement("SELECT count(*) AS rowcount FROM groups where groupapikey = ?")) {
            preparedStatement.setString(1, token);
            try(ResultSet r = preparedStatement.executeQuery()) {
                r.next();
                int count = r.getInt("rowcount");
                return count == 1;
            }
        }
    }
}
