package org.example.controller;

import org.example.models.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Scanner;

//annotation bean
// @Controller
// annotation bean
// @Controller
public class UserController {

    // annotation bean
    // @Autowired
    private UserService service;

    public UserController() {
    }

    // seeters and getters
    public void setService(UserService service) {
        this.service = service;
    }

    public void addUser() {
        System.out.println("adding user...");
        User user = new User(11, "Kate", 22, "pass@123");

        if (service.addUser(user) == null) {
            System.out.println("User not added: SQL Error");
        } else {
            System.out.println("User successfully added");
        }
    }

    public void getAllUsers() {
        System.out.println("Fetching all users...");
        List<User> users = service.getAllUsers();
        if (users == null || users.isEmpty()) {
            System.out.println("No users fetched");
        } else {
            for (User u : users) {
                System.out.println(u.getUsername() + " " + u.getAge());
            }
        }
    }

    public void updateUserAge() {
        System.out.println("Updating age... enter age & id");
        Scanner scanner = new Scanner(System.in);
        int age = scanner.nextInt();
        int userId = scanner.nextInt();

        User user = service.updateUserAge(age, userId);
        if (user != null) {
            System.out.println(user.getUsername() + " " + user.getAge());
        }
    }

    public void getOldestUserAge() {
        System.out.println("fetching oldest age...");
        Integer age = service.getOldestUserAge();
        if (age != null) {
            System.out.println("Oldest user's age is: " + age);
        } else {
            System.out.println("No users found");
        }
    }
}