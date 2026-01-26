package org.example.controller;

import org.example.models.User;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserController {

    private static final Logger logger =
            LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService service;

    public void addUser(){
        User user = new User(11,"Kate",22,"pass@123");

        if(service.addUser(user)==null){
            logger.error("User not added");
        }else{
            logger.info("User added successfully");
        }
    }

    public void getAllUsers(){
        List<User> users = service.getAllUsers();
        for(User u : users){
            logger.info(u.getUsername() + " " + u.getAge());
        }
    }

    public void getOldestUserAge(){
        Integer age = service.getOldestUserAge();
        logger.info("Oldest age: " + age);
    }
}