package com.revworkforce.service;

import com.revworkforce.dao.HolidayDAO;
import com.revworkforce.model.CompanyHoliday;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class HolidayService {
    private static final Logger logger = LogManager.getLogger(HolidayService.class);
    private HolidayDAO dao;

    public HolidayService() {
        this.dao = new HolidayDAO();
    }

    public HolidayService(HolidayDAO dao) {
        this.dao = dao;
    }

    public boolean addHoliday(CompanyHoliday holiday) {
        logger.info("Adding holiday: {} on {}", holiday.getName(), holiday.getHolidayDate());
        return dao.addHoliday(holiday);
    }

    public List<CompanyHoliday> getHolidays() {
        return dao.getAllHolidays();
    }
}
