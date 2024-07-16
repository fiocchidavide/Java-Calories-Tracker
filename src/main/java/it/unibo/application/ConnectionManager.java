package it.unibo.application;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionManager {
    private static final String dataBaseName = "mycalorietracker";
    private static final String protocol = "jdbc:mysql://localhost:3306/";
    private static final String URL = protocol + dataBaseName;
    private static final String user = "root";
    private static final String password = "cadmio";

    public static Connection getConnection(){
        try {
            return DriverManager.getConnection(URL, user, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
