package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    private DBConnection(){

    }
    private static Connection getConnection(){
        String url = "jdbc:mysql://localhost:3306/aposample_db";
        String user = "root";
        String password = "root";
        try{
            if(connection == null || connection.isClosed()){
                connection = DriverManager.getConnection(url, user, password);
            }
        }catch(SQLException se){
            System.out.println(se.getMessage());
        }
        return connection;
    }

    public static Connection getInstance(){
        return getConnection();
    }

}
