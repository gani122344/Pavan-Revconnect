package com.revworkforce.service;

import com.revworkforce.dao.PerformanceDAO;
import com.revworkforce.model.PerformanceReview;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PerformanceService {
    private static final Logger logger = LogManager.getLogger(PerformanceService.class);
    private final PerformanceDAO dao;
    private final com.revworkforce.dao.EmployeeDAO empDao = new com.revworkforce.dao.EmployeeDAO();
    private final NotificationService notificationService = new NotificationService();

    public PerformanceService() {
        this.dao = new PerformanceDAO();
    }

    public PerformanceService(PerformanceDAO dao) {
        this.dao = dao;
    }

    public boolean submitReview(PerformanceReview pr) {
        logger.info("Submitting review for emp: {}", pr.getEmployeeId());
        boolean submitted = dao.submitReview(pr);
        if (submitted) {
            try {
                com.revworkforce.model.Employee emp = empDao.getEmployeeById(pr.getEmployeeId());
                if (emp != null && emp.getManagerId() != 0) {
                    notificationService.sendNotification(emp.getManagerId(),
                            "Performance Review submitted by " + emp.getName());
                }
            } catch (Exception e) {
                logger.error("Failed to notify manager for review submission", e);
            }
        }
        return submitted;
    }

    public List<PerformanceReview> getTeamReviews(int managerId) {
        logger.info("Manager {} viewing team reviews", managerId);
        return dao.getTeamReviews(managerId);
    }

    public boolean giveFeedback(int reviewId, String feedback, int rating) {
        logger.info("Giving feedback for review {}", reviewId);
        boolean updated = dao.giveFeedback(reviewId, feedback, rating);
        if (updated) {
            try {
                PerformanceReview pr = dao.getReviewById(reviewId);
                if (pr != null) {
                    notificationService.sendNotification(pr.getEmployeeId(),
                            "Manager provided feedback on your review. Rating: " + rating);
                }
            } catch (Exception e) {
                logger.error("Failed to notify employee for feedback", e);
            }
        }
        return updated;
    }

    public List<PerformanceReview> getEmployeeReview(int employeeId) {
        return dao.getEmployeeReview(employeeId);
    }
}
