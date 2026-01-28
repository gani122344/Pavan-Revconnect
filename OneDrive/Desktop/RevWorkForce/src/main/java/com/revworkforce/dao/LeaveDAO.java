package com.revworkforce.dao;

import com.revworkforce.model.Leave;
import com.revworkforce.util.DBConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveDAO {
    private static final Logger logger = LogManager.getLogger(LeaveDAO.class);

    // Employee applies leave
    public boolean applyLeave(Leave leave) {

        String sql = """
                    INSERT INTO leave_request
                    (employee_id, leave_type, start_date, end_date, reason, status)
                    VALUES (?, ?, ?, ?, ?, 'PENDING')
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, leave.getEmployeeId());
            ps.setString(2, leave.getLeaveType());
            ps.setDate(3, leave.getStartDate());
            ps.setDate(4, leave.getEndDate());
            ps.setString(5, leave.getReason());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            logger.error("Error applying leave for empId: {}", leave.getEmployeeId(), e);
            e.printStackTrace();
            return false;
        }
    }

    // Manager views pending leaves
    public List<Leave> getPendingLeavesForManager(int managerId) {
        List<Leave> list = new ArrayList<>();
        String sql = """
                    SELECT lr.leave_id,
                           lr.employee_id,
                           e.name,
                           lr.leave_type,
                           lr.start_date,
                           lr.end_date,
                           lr.reason
                    FROM leave_request lr
                    JOIN employee e ON lr.employee_id = e.employee_id
                    WHERE lr.status = 'PENDING'
                      AND e.manager_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Leave l = new Leave();
                    l.setLeaveId(rs.getInt("leave_id"));
                    l.setEmployeeId(rs.getInt("employee_id"));
                    l.setEmployeeName(rs.getString("name")); // Populated from join
                    l.setLeaveType(rs.getString("leave_type"));
                    l.setStartDate(rs.getDate("start_date"));
                    l.setEndDate(rs.getDate("end_date"));
                    l.setReason(rs.getString("reason"));
                    l.setStatus("PENDING");
                    list.add(l);
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving pending leaves for managerId: {}", managerId, e);
            e.printStackTrace();
        }
        return list;
    }

    // Manager approves / rejects leave
    public boolean updateLeaveStatus(int leaveId, String status, String comment) {

        String sql = """
                    UPDATE leave_request
                    SET status = ?, manager_comment = ?
                    WHERE leave_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, comment);
            ps.setInt(3, leaveId);

            boolean updated = ps.executeUpdate() > 0;

            return updated;

        } catch (Exception e) {
            logger.error("Error updating leave status for leaveId: {}", leaveId, e);
            e.printStackTrace();
            return false;
        }
    }

    public Leave getLeaveById(int leaveId) {
        String sql = "SELECT * FROM leave_request WHERE leave_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, leaveId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Leave l = new Leave();
                l.setLeaveId(rs.getInt("leave_id"));
                l.setEmployeeId(rs.getInt("employee_id"));
                l.setLeaveType(rs.getString("leave_type"));
                l.setStartDate(rs.getDate("start_date"));
                l.setEndDate(rs.getDate("end_date"));
                l.setReason(rs.getString("reason"));
                l.setStatus(rs.getString("status"));
                l.setManagerComment(rs.getString("manager_comment"));
                return l;
            }
        } catch (Exception e) {
            logger.error("Error getting leave by ID: {}", leaveId, e);
            e.printStackTrace();
        }
        return null;
    }

    // Employee views leave status
    public List<Leave> getLeavesByEmployee(int employeeId) {
        List<Leave> list = new ArrayList<>();
        String sql = """
                    SELECT leave_id,
                           leave_type,
                           start_date,
                           end_date,
                           status,
                           manager_comment
                    FROM leave_request
                    WHERE employee_id = ?
                    ORDER BY applied_on DESC
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Leave l = new Leave();
                    l.setLeaveId(rs.getInt("leave_id"));
                    // l.setEmployeeId(employeeId); // Not strictly needed for list view
                    l.setLeaveType(rs.getString("leave_type"));
                    l.setStartDate(rs.getDate("start_date"));
                    l.setEndDate(rs.getDate("end_date"));
                    l.setStatus(rs.getString("status"));
                    l.setManagerComment(rs.getString("manager_comment"));
                    list.add(l);
                }
            }
        } catch (Exception e) {
            logger.error("Error getting leaves for employeeId: {}", employeeId, e);
            e.printStackTrace();
        }
        return list;
    }

    // Employee views leave balance
    public Map<String, Integer> getLeaveBalance(int employeeId) {
        Map<String, Integer> balance = new HashMap<>();
        String sql = "SELECT cl, sl, pl FROM leave_balance WHERE employee_id=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    balance.put("CL", rs.getInt("cl"));
                    balance.put("SL", rs.getInt("sl"));
                    balance.put("PL", rs.getInt("pl"));
                }
            }
        } catch (Exception e) {
            logger.error("Error getting leave balance for employeeId: {}", employeeId, e);
            e.printStackTrace();
        }
        return balance;
    }

    // Helper method
    // Manager views approved leaves (Team Calendar)
    public List<Leave> getApprovedLeavesForManager(int managerId) {
        List<Leave> list = new ArrayList<>();
        String sql = """
                    SELECT lr.leave_id,
                           lr.employee_id,
                           e.name,
                           lr.leave_type,
                           lr.start_date,
                           lr.end_date
                    FROM leave_request lr
                    JOIN employee e ON lr.employee_id = e.employee_id
                    WHERE lr.status = 'APPROVED'
                      AND e.manager_id = ?
                    ORDER BY lr.start_date
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Leave l = new Leave();
                    l.setLeaveId(rs.getInt("leave_id"));
                    l.setEmployeeId(rs.getInt("employee_id"));
                    l.setEmployeeName(rs.getString("name"));
                    l.setLeaveType(rs.getString("leave_type"));
                    l.setStartDate(rs.getDate("start_date"));
                    l.setEndDate(rs.getDate("end_date"));
                    l.setStatus("APPROVED");
                    list.add(l);
                }
            }
        } catch (Exception e) {
            logger.error("Error getting approved leaves for managerId: {}", managerId, e);
            e.printStackTrace();
        }
        return list;
    }

    // =============================
    // Employee cancels pending leave
    // =============================
    public boolean cancelPendingLeave(int leaveId, int employeeId) {
        String sql = "UPDATE leave_request SET status='CANCELLED' WHERE leave_id=? AND employee_id=? AND status='PENDING'";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, leaveId);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Error cancelling pending leave for leaveId: {}", leaveId, e);
            e.printStackTrace();
            return false;
        }
    }

    // Admin: Get Leave Statistics
    public List<Map<String, Object>> getLeaveStatistics() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                    SELECT e.department, lr.leave_type, COUNT(*) as count
                    FROM leave_request lr
                    JOIN employee e ON lr.employee_id = e.employee_id
                    GROUP BY e.department, lr.leave_type
                """;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("department", rs.getString("department"));
                map.put("leave_type", rs.getString("leave_type"));
                map.put("count", rs.getInt("count"));
                list.add(map);
            }
        } catch (Exception e) {
            logger.error("Error getting leave statistics", e);
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, Object>> getTeamAttendance(int managerId) {
        List<Map<String, Object>> list = new ArrayList<>();
        // Check for today
        String sql = """
                    SELECT e.employee_id, e.name,
                           CASE WHEN lr.leave_id IS NOT NULL THEN 'ON LEAVE' ELSE 'PRESENT' END as status,
                           lr.leave_type
                    FROM employee e
                    LEFT JOIN leave_request lr ON e.employee_id = lr.employee_id
                         AND CURRENT_DATE BETWEEN lr.start_date AND lr.end_date
                         AND lr.status = 'APPROVED'
                    WHERE e.manager_id = ?
                """;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("employee_id", rs.getInt("employee_id"));
                    map.put("name", rs.getString("name"));
                    map.put("status", rs.getString("status"));
                    map.put("leave_type", rs.getString("leave_type"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            logger.error("Error getting team attendance for managerId: {}", managerId, e);
            e.printStackTrace();
        }
        return list;
    }
}
