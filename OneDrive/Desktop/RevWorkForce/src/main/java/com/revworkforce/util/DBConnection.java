package com.revworkforce.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String PROPERTIES_FILE = "application.properties";

    public static Connection getConnection() {
        try {
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
                if (input == null) {
                    System.out.println("Sorry, unable to find " + PROPERTIES_FILE);
                    return null;
                }
                props.load(input);
            }

            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
