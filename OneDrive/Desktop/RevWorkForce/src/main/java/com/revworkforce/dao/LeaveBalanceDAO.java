package com.revworkforce.dao;

import com.revworkforce.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveBalanceDAO {

    // Get balance for employee
    public Map<String, Integer> getBalance(int employeeId) {
        Map<String, Integer> balance = new HashMap<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM leave_balance WHERE employee_id=?")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    balance.put("CL", rs.getInt("cl"));
                    balance.put("SL", rs.getInt("sl"));
                    balance.put("PL", rs.getInt("pl"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance;
    }

    // Deduct leave after approval
    public boolean deductLeave(int employeeId, String leaveType, int days) {

        String column = switch (leaveType) {
            case "CL" -> "cl";
            case "SL" -> "sl";
            case "PL" -> "pl";
            default -> null;
        };

        if (column == null)
            return false;

        String sql = "UPDATE leave_balance SET " + column + " = " + column + " - ? WHERE employee_id=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, days);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Admin adjusts leave balance
    public boolean updateBalance(int employeeId, int cl, int sl, int pl) {
        String sql = "UPDATE leave_balance SET cl=?, sl=?, pl=? WHERE employee_id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cl);
            ps.setInt(2, sl);
            ps.setInt(3, pl);
            ps.setInt(4, employeeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Manager views team balances
    public List<Map<String, Object>> getTeamBalances(int managerId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                SELECT e.name, b.cl, b.sl, b.pl
                FROM leave_balance b
                JOIN employee e ON b.employee_id = e.employee_id
                WHERE e.manager_id = ?
                """;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", rs.getString("name"));
                    map.put("cl", rs.getInt("cl"));
                    map.put("sl", rs.getInt("sl"));
                    map.put("pl", rs.getInt("pl"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
