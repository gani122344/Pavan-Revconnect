package org.example;

import org.example.config.AppConfig;
import org.example.controller.UserController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {

        // 1. xml way content
        ApplicationContext context = new ClassPathXmlApplicationContext("ApplicationBasedConfiguration.xml");

        // java config context 2 3
        // Commented 1 and uncomment below to use 2 or 3
        // ApplicationContext context = new
        // AnnotationConfigApplicationContext(AppConfig.class);

        // Get Bean from context
        UserController controller = context.getBean(UserController.class);

        // Execute methods
        controller.addUser();
        controller.getAllUsers();
        controller.getOldestUserAge();
    }
}