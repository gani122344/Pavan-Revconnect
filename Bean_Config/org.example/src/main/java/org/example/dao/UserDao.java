package org.example.dao;

import org.example.config.DBConnection;
import org.example.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//annotation bean
// @Repository
public class UserDao {

    private Connection conn;

    public UserDao() {
        this.conn = DBConnection.getInstance();
    }

    public User addUser(User user) {
        String sql = "INSERT INTO users VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setInt(3, user.getAge());
            ps.setString(4, user.getPassword());

            if (ps.executeUpdate() > 0) {
                return user;
            }
        } catch (SQLException se) {
            System.out.println(se.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setAge(rs.getInt("age"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                users.add(user);
            }
            return users;
        } catch (SQLException se) {
            System.out.println(se);
        }
        return null;
    }

    public User updateAge(int age, int userId) {
        String sql = "UPDATE users SET age = ? WHERE user_id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, age);
            ps.setInt(2, userId);
            if (ps.executeUpdate() > 0) {
                System.out.println("UPDATE SUCCESSFULL");
                return getUserById(userId);
            }
        } catch (SQLException se) {
            System.out.println(se.getMessage());
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            User user = new User();

            if (rs.next()) {
                System.out.println("USER FETCHED: " + rs.getString("username"));
                user.setUserId(rs.getInt("user_id"));
                user.setAge(rs.getInt("age"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
            }
            return user;
        } catch (SQLException se) {
            System.out.println(se.getMessage());
        }
        return null;
    }

    public Integer getOldestUserAge() {
        String sql = "SELECT MAX(age) AS max_age FROM users";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int age = rs.getInt("max_age");
                return rs.wasNull() ? null : age;
            }
        } catch (SQLException se) {
            System.out.println(se.getMessage());
        }
        return null;
    }
}