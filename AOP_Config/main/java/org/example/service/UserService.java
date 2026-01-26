package org.example.service;

import org.example.dao.UserDao;
import org.example.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserDao dao;

    public User addUser(User user){
        return dao.addUser(user);
    }

    public List<User> getAllUsers(){
        return dao.getAllUsers();
    }

    public Integer getOldestUserAge(){
        return dao.getOldestUserAge();
    }
}