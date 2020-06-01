package sample.databasemanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManager {
    private static final String USERNAME = "st58310";
    private static final String URL = "jdbc:oracle:thin:@fei-sql1.upceucebny.cz:1521:IDAS";
    private static final String PASSWORD = "idas1";
    private Connection connection;

    public DbManager() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
