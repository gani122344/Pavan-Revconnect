package com.revworkforce.service;

import com.revworkforce.dao.AuditLogDAO;
import java.util.List;
import java.util.Map;

public class AuditLogService {
    private AuditLogDAO dao;

    public AuditLogService() {
        this.dao = new AuditLogDAO();
    }

    public AuditLogService(AuditLogDAO dao) {
        this.dao = dao;
    }

    public boolean logAction(String action, int performedBy, String details) {
        return dao.log(action, performedBy, details);
    }

    public List<Map<String, Object>> getSystemLogs() {
        return dao.getAllLogs();
    }
}
