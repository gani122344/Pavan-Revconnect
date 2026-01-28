package com.revworkforce.service;

import com.revworkforce.dao.NotificationDAO;
import com.revworkforce.model.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class NotificationService {
    private static final Logger logger = LogManager.getLogger(NotificationService.class);
    private NotificationDAO dao;

    public NotificationService() {
        this.dao = new NotificationDAO();
    }

    public NotificationService(NotificationDAO dao) {
        this.dao = dao;
    }

    public List<Notification> getNotifications(int employeeId) {
        return dao.getNotifications(employeeId);
    }

    public void markAsRead(int employeeId) {
        dao.markAsRead(employeeId);
    }

    public void sendNotification(int employeeId, String message) {
        logger.info("Sending notification to employee ID: {}", employeeId);
        dao.addNotification(employeeId, message);
    }
}
