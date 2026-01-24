package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;

    private DBConnection() {

    }

    private static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/social_media";
        String user = "root";
        String password = "Revature@123";
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException se) {
            System.out.println(se);
        }
        return connection;

    }

    public static Connection getInstance() {
        return getConnection();
    }
}