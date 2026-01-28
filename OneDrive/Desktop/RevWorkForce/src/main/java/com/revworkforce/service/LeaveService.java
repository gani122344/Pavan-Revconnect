package com.revworkforce.service;

import com.revworkforce.dao.LeaveDAO;
import com.revworkforce.model.Leave;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LeaveService {

    private static final Logger logger = LogManager.getLogger(LeaveService.class);
    private LeaveDAO dao;
    private com.revworkforce.dao.EmployeeDAO empDao = new com.revworkforce.dao.EmployeeDAO();
    private NotificationService notificationService = new NotificationService();
    private final com.revworkforce.dao.LeaveBalanceDAO balanceDAO = new com.revworkforce.dao.LeaveBalanceDAO();

    public LeaveService() {
        this.dao = new LeaveDAO();
    }

    public LeaveService(LeaveDAO dao) {
        this.dao = dao;
    }

    // Employee applies leave
    public boolean applyLeave(Leave leave) {
        boolean applied = dao.applyLeave(leave);
        if (applied) {
            try {
                com.revworkforce.model.Employee emp = empDao.getEmployeeById(leave.getEmployeeId());
                if (emp != null && emp.getManagerId() != 0) {
                    notificationService.sendNotification(emp.getManagerId(),
                            "Leave applied by " + emp.getName() + " (" + leave.getLeaveType() + ")");
                }
            } catch (Exception e) {
                logger.error("Failed to send notification for leave application", e);
            }
        }
        return applied;
    }

    // Manager views pending leaves
    public List<Leave> getPendingLeaves(int managerId) {
        return dao.getPendingLeavesForManager(managerId);
    }

    // Manager approves or rejects leave
    public boolean approveOrRejectLeave(int leaveId, String status, String comment) {
        boolean updated = dao.updateLeaveStatus(leaveId, status, comment);
        if (updated) {
            try {
                Leave l = dao.getLeaveById(leaveId);
                if (l != null) {
                    notificationService.sendNotification(l.getEmployeeId(),
                            "Your leave request was " + status + ". Comment: " + comment);
                }
            } catch (Exception e) {
                logger.error("Failed to send notification for leave update", e);
            }
        }
        return updated;
    }

    // Employee views leave status
    public List<Leave> getLeavesByEmployee(int employeeId) {
        return dao.getLeavesByEmployee(employeeId);
    }

    // Employee views leave balance
    public Map<String, Integer> getLeaveBalance(int employeeId) {
        return dao.getLeaveBalance(employeeId); // Delegate to DAO (now returns Map)
        // Wait, LeaveDAO.getLeaveBalance returns Map<String, Integer>.
        // BUT my refactor in step 332 updated LeaveDAO to return Map<String, Integer>.
        // Wait, looking at Step 332...
        // public Map<String, Integer> getLeaveBalance(int employeeId) { ... }
        // YES.
        // But LeaveService.java previously delegated to it.
        // Wait, LeaveDAO.getLeaveBalance was refactored in Step 332.
        // So this is correct.
    }

    // Manager views team calendar (approved leaves)
    public List<Leave> getApprovedLeaves(int managerId) {
        return dao.getApprovedLeavesForManager(managerId);
    }

    // Admin updates balance
    public boolean updateLeaveBalance(int empId, int cl, int sl, int pl) {
        return balanceDAO.updateBalance(empId, cl, sl, pl);
    }

    // Manager views team balances
    public List<Map<String, Object>> getTeamBalances(int managerId) {
        logger.info("Manager {} viewing team balances", managerId);
        return balanceDAO.getTeamBalances(managerId);
    }

    public boolean cancelPendingLeave(int leaveId, int employeeId) {
        logger.info("Employee {} cancelling leave {}", employeeId, leaveId);
        return dao.cancelPendingLeave(leaveId, employeeId);
    }

    public boolean revokeLeave(int leaveId) {
        logger.info("Admin revoking leave {}", leaveId);
        return dao.updateLeaveStatus(leaveId, "REVOKED", "Revoked by Admin");
    }

    public List<Map<String, Object>> getLeaveStatistics() {
        return dao.getLeaveStatistics();
    }

    public List<Map<String, Object>> getTeamAttendance(int managerId) {
        return dao.getTeamAttendance(managerId);
    }
}
