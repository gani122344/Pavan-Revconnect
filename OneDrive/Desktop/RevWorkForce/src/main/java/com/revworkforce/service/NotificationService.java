package com.revworkforce.service;

import com.revworkforce.dao.NotificationDAO;
import com.revworkforce.model.Notification;

import java.util.List;

public class NotificationService {
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
        dao.addNotification(employeeId, message);
    }
}
