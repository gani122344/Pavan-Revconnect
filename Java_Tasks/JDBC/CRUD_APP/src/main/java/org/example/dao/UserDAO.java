package Java.JDBC.CRUD_APP.src.main.java.org.example.dao;

import java.sql.*;

import Java.JDBC.CRUD_APP.src.main.java.org.example.config.DBConnection;
import Java.JDBC.CRUD_APP.src.main.java.org.example.model.User;

public class UserDAO {

    public boolean registerUser(User user) {

        String sql = "INSERT INTO users(username, password, email, status) VALUES (?, ?, ?, ?)";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, "ACTIVE");

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Registration Error: " + e.getMessage());
            return false;
        }
    }

    public User login(String username, String password) {

        String sql = "SELECT * FROM users WHERE username=? AND password=? AND status='ACTIVE'";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setStatus(rs.getString("status"));
                return user;
            }

        } catch (Exception e) {
            System.out.println("Login Error: " + e.getMessage());
        }
        return null;
    }

    public User getUserById(int id) {

        String sql = "SELECT * FROM users WHERE id=?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(id);
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setStatus(rs.getString("status"));
                return user;
            }

        } catch (Exception e) {
            System.out.println("Profile Error: " + e.getMessage());
        }
        return null;
    }

    public boolean updateProfile(int id, String username, String email) {

        String sql = "UPDATE users SET username=?, email=? WHERE id=?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setInt(3, id);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Profile Update Error: " + e.getMessage());
            return false;
        }
    }

    public boolean changePassword(int id, String oldPass, String newPass) {

        String checkSql = "SELECT * FROM users WHERE id=? AND password=?";

        String updateSql = "UPDATE users SET password=? WHERE id=?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement checkPs = con.prepareStatement(checkSql);

            checkPs.setInt(1, id);
            checkPs.setString(2, oldPass);

            ResultSet rs = checkPs.executeQuery();
            if (!rs.next())
                return false;

            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setString(1, newPass);
            updatePs.setInt(2, id);
            updatePs.executeUpdate();

            return true;

        } catch (Exception e) {
            System.out.println("Change Password Error: " + e.getMessage());
            return false;
        }
    }

    public boolean resetPassword(String username, String newPass) {

        String sql = "UPDATE users SET password=? WHERE username=?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, newPass);
            ps.setString(2, username);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Reset Password Error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {

        String sql = "UPDATE users SET status=? WHERE id=?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, status);
            ps.setInt(2, id);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Status Update Error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int id) {

        String sql = "DELETE FROM users WHERE id=?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Delete Account Error: " + e.getMessage());
            return false;
        }
    }

}
