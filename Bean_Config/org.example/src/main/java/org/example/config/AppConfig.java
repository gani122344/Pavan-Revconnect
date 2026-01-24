package org.example.config;

import org.example.controller.UserController;
import org.example.dao.UserDao;
import org.example.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
// for annotation beans
// @ComponentScan(base-package = "org.example")
public class AppConfig {

    @Bean
    public UserDao userDao() {
        return new UserDao();
    }

    @Bean
    public UserService userService() {
        UserService service = new UserService();
        service.setDao(userDao()); // Manual Injection
        return service;
    }

    @Bean
    public UserController userController() {
        UserController controller = new UserController();
        controller.setService(userService()); // Manual Injection
        return controller;
    }
}