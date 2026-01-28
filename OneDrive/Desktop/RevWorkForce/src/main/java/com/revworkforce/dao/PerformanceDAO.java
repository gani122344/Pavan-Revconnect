package com.revworkforce.dao;

import com.revworkforce.model.PerformanceReview;
import com.revworkforce.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PerformanceDAO {

    // Employee submits review
    public boolean submitReview(PerformanceReview pr) {

        String sql = """
                    INSERT INTO performance_review
                    (employee_id, year, self_assessment, achievements,
                     improvement_areas, status)
                    VALUES (?, ?, ?, ?, ?, 'SUBMITTED')
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pr.getEmployeeId());
            ps.setInt(2, pr.getYear());
            ps.setString(3, pr.getSelfAssessment());
            ps.setString(4, pr.getAchievements());
            ps.setString(5, pr.getImprovementAreas());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Manager views team reviews
    public List<PerformanceReview> getTeamReviews(int managerId) {
        List<PerformanceReview> list = new ArrayList<>();
        String sql = """
                    SELECT pr.review_id, e.employee_id, e.name,
                           pr.year, pr.status, pr.rating
                    FROM performance_review pr
                    JOIN employee e ON pr.employee_id = e.employee_id
                    WHERE e.manager_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PerformanceReview pr = new PerformanceReview();
                    pr.setReviewId(rs.getInt("review_id"));
                    pr.setEmployeeId(rs.getInt("employee_id"));
                    pr.setEmployeeName(rs.getString("name")); // Using new field
                    pr.setYear(rs.getInt("year"));
                    pr.setStatus(rs.getString("status"));
                    pr.setRating(rs.getInt("rating"));
                    list.add(pr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Manager gives feedback
    public boolean giveFeedback(int reviewId, String feedback, int rating) {

        String sql = """
                    UPDATE performance_review
                    SET manager_feedback=?, rating=?, status='REVIEWED'
                    WHERE review_id=?
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, feedback);
            ps.setInt(2, rating);
            ps.setInt(3, reviewId);

            boolean updated = ps.executeUpdate() > 0;
            return updated;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public PerformanceReview getReviewById(int reviewId) {
        String sql = "SELECT * FROM performance_review WHERE review_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PerformanceReview pr = new PerformanceReview();
                pr.setReviewId(rs.getInt("review_id"));
                pr.setEmployeeId(rs.getInt("employee_id"));
                pr.setYear(rs.getInt("year"));
                pr.setSelfAssessment(rs.getString("self_assessment"));
                pr.setAchievements(rs.getString("achievements"));
                pr.setImprovementAreas(rs.getString("improvement_areas"));
                pr.setManagerFeedback(rs.getString("manager_feedback"));
                pr.setRating(rs.getInt("rating"));
                pr.setStatus(rs.getString("status"));
                return pr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Employee views own review
    public List<PerformanceReview> getEmployeeReview(int employeeId) {
        List<PerformanceReview> list = new ArrayList<>();
        String sql = """
                    SELECT year, status, manager_feedback, rating
                    FROM performance_review
                    WHERE employee_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PerformanceReview pr = new PerformanceReview();
                    // pr.setEmployeeId(employeeId);
                    pr.setYear(rs.getInt("year"));
                    pr.setStatus(rs.getString("status"));
                    pr.setManagerFeedback(rs.getString("manager_feedback"));
                    pr.setRating(rs.getInt("rating"));
                    list.add(pr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
