package com.revworkforce.dao;

import com.revworkforce.model.Notification;
import com.revworkforce.util.DBConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private static final Logger logger = LogManager.getLogger(NotificationDAO.class);

    // Add notification
    public void addNotification(int employeeId, String message) {
        String sql = "INSERT INTO notification (employee_id, message) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setString(2, message);
            ps.executeUpdate();

        } catch (Exception e) {
            logger.error("Error adding notification for empId: {}", employeeId, e);
            e.printStackTrace();
        }
    }

    // Get notifications
    public List<Notification> getNotifications(int employeeId) {
        List<Notification> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM notification WHERE employee_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setEmployeeId(rs.getInt("employee_id"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving notifications for empId: {}", employeeId, e);
            e.printStackTrace();
        }
        return list;
    }

    // Mark all as read
    public void markAsRead(int employeeId) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE notification SET is_read=TRUE WHERE employee_id=?")) {

            ps.setInt(1, employeeId);
            ps.executeUpdate();

        } catch (Exception e) {
            logger.error("Error marking notifications as read for empId: {}", employeeId, e);
            e.printStackTrace();
        }
    }
}
