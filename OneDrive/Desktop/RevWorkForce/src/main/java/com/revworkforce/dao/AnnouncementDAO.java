package com.revworkforce.dao;

import com.revworkforce.model.Announcement;
import com.revworkforce.util.DBConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO {

    private static final Logger logger = LogManager.getLogger(AnnouncementDAO.class);

    public boolean addAnnouncement(Announcement ann) {
        String sql = "INSERT INTO announcement (message, posted_by, posted_date) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ann.getMessage());
            ps.setInt(2, ann.getPostedBy());
            ps.setDate(3, ann.getPostedDate());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Error adding announcement", e);
            e.printStackTrace();
            return false;
        }
    }

    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM announcement ORDER BY posted_date DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Announcement a = new Announcement();
                a.setMessage(rs.getString("message"));
                a.setPostedBy(rs.getInt("posted_by"));
                a.setPostedDate(rs.getDate("posted_date"));
                list.add(a);
            }
        } catch (Exception e) {
            logger.error("Error retrieving announcements", e);
            e.printStackTrace();
        }
        return list;
    }
}
