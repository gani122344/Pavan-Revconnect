package org.example.service;

import org.example.dao.UserDao;
import org.example.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//  annotation beans
// @Service
// annotation beans
// @Service
public class UserService {

    // 3 annotation way
    // @Autowired
    private UserDao dao;

    public UserService() {
    }

    // setter injection to
    public void setDao(UserDao dao) {
        this.dao = dao;
    }

    public User addUser(User user) {
        return dao.addUser(user);
    }

    public List<User> getAllUsers() {
        return dao.getAllUsers();
    }

    public User updateUserAge(int age, int userId) {
        return dao.updateAge(age, userId);
    }

    public Integer getOldestUserAge() {
        return dao.getOldestUserAge();
    }
}