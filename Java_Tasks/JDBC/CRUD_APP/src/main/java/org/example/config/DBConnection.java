package Java.JDBC.CRUD_APP.src.main.java.org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/crud_app";
    private static final String USER = "root";
    private static final String PASSWORD = "Revature@123";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
