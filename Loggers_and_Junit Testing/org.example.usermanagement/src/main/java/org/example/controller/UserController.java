package org.example.controller;

import org.example.model.User;
import org.example.service.UserService;

import java.util.Scanner;

public class UserController {

    private UserService service;
    private Scanner sc = new Scanner(System.in);
    public UserController() {
        service = new UserService();
    }
    public void register() {
        System.out.println("Enter Name, Email, Age, Phone, Password");
        User user = new User(
                sc.next(),
                sc.next(),
                sc.nextInt(),
                sc.next(),
                sc.next(),
                "active"
        );
        System.out.println(service.register(user) ? "Registration Successful" : "Registration Failed");
    }

    public User login() {
        System.out.println("Enter Email & Password");
        User user = service.login(sc.next(), sc.next());
        System.out.println(user != null ? "Login Success" : "Invalid Credentials");
        return user;
    }

    public void updateProfile(User user) {
        System.out.println("Enter new Name, Age, Phone");
        user.setName(sc.next());
        user.setAge(sc.nextInt());
        user.setPhone(sc.next());
        System.out.println(service.updateProfile(user) ? "Profile Updated" : "Update Failed");
    }
    public void changePassword(User user) {
        System.out.println("Enter new password");
        System.out.println(service.changePassword(user.getUserId(), sc.next()) ? "Password Updated" : "Failed");
    }
    public void resetPassword() {
        System.out.println("Enter Email & New Password");
        System.out.println(service.resetPassword(sc.next(), sc.next()) ? "Password Reset" : "Failed");
    }
    public void deleteAccount(User user) {
        System.out.println(service.deleteAccount(user.getUserId()) ? "Account Deleted" : "Failed");
    }
    public void viewProfile(User user) {
        System.out.println("------ USER PROFILE ------");
        System.out.println("ID    : " + user.getUserId());
        System.out.println("Name  : " + user.getName());
        System.out.println("Email : " + user.getEmail());
        System.out.println("Age   : " + user.getAge());
        System.out.println("Phone : " + user.getPhone());
        System.out.println("Status: " + user.getStatus());
    }

}

