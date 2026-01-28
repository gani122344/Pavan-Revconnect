package com.revworkforce.util;

import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBConnection {
    private static final Logger logger = LogManager.getLogger(DBConnection.class);

    private static final String PROPERTIES_FILE = "application.properties";

    public static Connection getConnection() {
        try {
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
                if (input == null) {
                    logger.error("Sorry, unable to find {}", PROPERTIES_FILE);

                    return null;
                }
                props.load(input);
            }

            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password"));
        } catch (Exception e) {
            logger.error("Database Connection Error", e);
            e.printStackTrace();
            return null;
        }
    }
}
