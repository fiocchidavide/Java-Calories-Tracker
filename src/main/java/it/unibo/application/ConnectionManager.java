package it.unibo.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String dataBaseName = "mycalorietracker";
    private static final String protocol = "jdbc:mysql://localhost:3306/";
    private static final String URL = protocol + dataBaseName;
    private static final String user = "root";
    private static final String password = "cadmio";

    public static Connection getConnection(){
        return null;
        /*
        try {
            return DriverManager.getConnection(URL, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
            */
    }
}
