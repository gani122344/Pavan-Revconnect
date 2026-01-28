package com.revworkforce.dao;

import com.revworkforce.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuditLogDAO {

    public boolean log(String action, int performedBy, String details) {
        String sql = "INSERT INTO audit_log (action, performed_by, details) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setInt(2, performedBy);
            ps.setString(3, details);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
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
            e.printStackTrace();
        }
        return list;
    }
}
