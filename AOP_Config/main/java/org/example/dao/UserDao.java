package org.example.dao;

import org.example.config.DBConnection;
import org.example.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDao {

    private static final Logger logger =
            LoggerFactory.getLogger(UserDao.class);

    private Connection conn = DBConnection.getInstance();

    public User addUser(User user){
        try{
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users VALUES (?,?,?,?)");
            ps.setInt(1,user.getUser_id());
            ps.setString(2,user.getUsername());
            ps.setInt(3,user.getAge());
            ps.setString(4,user.getPassword());
            ps.executeUpdate();
            return user;
        }catch(Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    public List<User> getAllUsers(){
        List<User> users = new ArrayList<>();
        try{
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM users");
            while(rs.next()){
                User u = new User();
                u.setUser_id(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setAge(rs.getInt("age"));
                u.setPassword(rs.getString("password"));
                users.add(u);
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return users;
    }

    public Integer getOldestUserAge(){
        try{
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT MAX(age) max_age FROM users");
            if(rs.next()) return rs.getInt("max_age");
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }
}