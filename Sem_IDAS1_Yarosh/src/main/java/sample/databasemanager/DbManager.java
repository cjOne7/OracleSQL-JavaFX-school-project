package sample.databasemanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.enums.CommentColumns;

import java.sql.*;

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

    public static void removeRemainingDataFromDB(final String selectQuery, final String deleteQuery, final String columnName) throws SQLException {
        final DbManager dbManager = new DbManager();
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        final ResultSet resultSet = preparedStatement.executeQuery();
        final ObservableList<Integer> unusedIds = FXCollections.observableArrayList();
        while (resultSet.next()) {
            unusedIds.add(resultSet.getInt(columnName));
        }
        final PreparedStatement deleteStatement = dbManager.getConnection().prepareStatement(deleteQuery);
        for (int i = 0; i < unusedIds.size(); i++) {
            deleteStatement.setInt(1, unusedIds.get(i));
            deleteStatement.execute();
        }
    }
}
