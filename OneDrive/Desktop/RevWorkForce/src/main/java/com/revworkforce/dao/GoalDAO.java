package com.revworkforce.dao;

import com.revworkforce.model.Goal;
import com.revworkforce.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalDAO {

    // Employee creates goal
    public boolean createGoal(Goal g) {

        String sql = """
                    INSERT INTO performance_goal
                    (employee_id, goal_description, deadline, priority,
                     success_metrics, progress, status)
                    VALUES (?, ?, ?, ?, ?, 0, 'IN_PROGRESS')
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, g.getEmployeeId());
            ps.setString(2, g.getGoalDescription());
            ps.setDate(3, g.getDeadline());
            ps.setString(4, g.getPriority());
            ps.setString(5, g.getSuccessMetrics());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Employee views goals
    public List<Goal> getGoalsByEmployee(int empId) {
        List<Goal> list = new ArrayList<>();
        String sql = """
                    SELECT goal_id, goal_description, deadline,
                           priority, progress, status
                    FROM performance_goal
                    WHERE employee_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Goal g = new Goal();
                    g.setGoalId(rs.getInt("goal_id"));
                    g.setGoalDescription(rs.getString("goal_description"));
                    g.setDeadline(rs.getDate("deadline"));
                    g.setPriority(rs.getString("priority"));
                    g.setProgress(rs.getInt("progress"));
                    g.setStatus(rs.getString("status"));
                    list.add(g);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Update progress
    public boolean updateProgress(int goalId, int progress) {

        String sql = """
                    UPDATE performance_goal
                    SET progress=?, status=IF(?=100,'COMPLETED','IN_PROGRESS')
                    WHERE goal_id=?
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, progress);
            ps.setInt(2, progress);
            ps.setInt(3, goalId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Manager views team goals
    public List<Map<String, Object>> getTeamGoals(int managerId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                    SELECT g.goal_id, e.name, g.goal_description,
                           g.progress, g.status
                    FROM performance_goal g
                    JOIN employee e ON g.employee_id = e.employee_id
                    WHERE e.manager_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("goal_id", rs.getInt("goal_id"));
                    map.put("name", rs.getString("name"));
                    map.put("goal_description", rs.getString("goal_description"));
                    map.put("progress", rs.getInt("progress"));
                    map.put("status", rs.getString("status"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
