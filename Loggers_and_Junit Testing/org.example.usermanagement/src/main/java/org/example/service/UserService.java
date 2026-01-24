package org.example.service;

import org.example.dao.UserDao;
import org.example.model.User;

public class UserService {

    private UserDao dao;

    public UserService() {
        this.dao = new UserDao();
    }
    public boolean register(User user) {
        return dao.register(user);
    }
    public User login(String email, String password) {
        return dao.login(email, password);
    }

    public boolean updateProfile(User user) {
        return dao.updateProfile(user);
    }
    public boolean resetPassword(String email, String newPassword) {
        return dao.resetPassword(email, newPassword);
    }
    public boolean changePassword(int userId, String newPassword) {
        return dao.changePassword(userId, newPassword);
    }
    public boolean deleteAccount(int userId) {
        return dao.deleteAccount(userId);
    }
}

