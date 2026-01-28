package com.revworkforce;

import com.revworkforce.controller.AuthController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Application starting...");
        while (true) {
            if (!AuthController.show()) {
                System.out.println("Exiting Application. Goodbye!");
                logger.info("Application exiting.");
                break;
            }
        }
    }
}
