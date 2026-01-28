package com.revworkforce.service;

import com.revworkforce.dao.AuditLogDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Map;

public class AuditLogService {
    private static final Logger logger = LogManager.getLogger(AuditLogService.class);
    private AuditLogDAO dao;

    public AuditLogService() {
        this.dao = new AuditLogDAO();
    }

    public AuditLogService(AuditLogDAO dao) {
        this.dao = dao;
    }

    public boolean logAction(String action, int performedBy, String details) {
        logger.info("Logging action: {}, User: {}", action, performedBy);
        return dao.log(action, performedBy, details);
    }

    public List<Map<String, Object>> getSystemLogs() {
        logger.info("Fetching system logs");
        return dao.getAllLogs();
    }
}
