package com.revworkforce.dao;

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

public class AuditLogDAO {

    private static final Logger logger = LogManager.getLogger(AuditLogDAO.class);

    public boolean log(String action, int performedBy, String details) {
        String sql = "INSERT INTO audit_log (action, performed_by, details) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setInt(2, performedBy);
            ps.setString(3, details);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Error logging action: {}", action, e);
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getAllLogs() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM audit_log ORDER BY timestamp DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("timestamp", rs.getTimestamp("timestamp"));
                map.put("performed_by", rs.getInt("performed_by"));
                map.put("action", rs.getString("action"));
                map.put("details", rs.getString("details"));
                list.add(map);
            }
        } catch (Exception e) {
            logger.error("Error retrieving audit logs", e);
            e.printStackTrace();
        }
        return list;
    }
}
