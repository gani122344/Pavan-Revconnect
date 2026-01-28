package com.revworkforce.dao;

import com.revworkforce.model.CompanyHoliday;
import com.revworkforce.util.DBConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HolidayDAO {
    private static final Logger logger = LogManager.getLogger(HolidayDAO.class);

    public boolean addHoliday(CompanyHoliday holiday) {
        String sql = "INSERT INTO company_holiday (holiday_date, name, type) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, holiday.getHolidayDate());
            ps.setString(2, holiday.getName());
            ps.setString(3, holiday.getType());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            logger.error("Error adding holiday", e);
            e.printStackTrace();
            return false;
        }
    }

    public List<CompanyHoliday> getAllHolidays() {
        List<CompanyHoliday> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM company_holiday ORDER BY holiday_date");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CompanyHoliday h = new CompanyHoliday();
                h.setHolidayDate(rs.getDate("holiday_date"));
                h.setName(rs.getString("name"));
                h.setType(rs.getString("type"));
                list.add(h);
            }
        } catch (Exception e) {
            logger.error("Error retrieving holidays", e);
            e.printStackTrace();
        }
        return list;
    }
}
