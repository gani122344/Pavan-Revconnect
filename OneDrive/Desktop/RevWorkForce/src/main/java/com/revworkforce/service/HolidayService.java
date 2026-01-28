package com.revworkforce.service;

import com.revworkforce.dao.HolidayDAO;
import com.revworkforce.model.CompanyHoliday;

import java.util.List;

public class HolidayService {
    private HolidayDAO dao;

    public HolidayService() {
        this.dao = new HolidayDAO();
    }

    public HolidayService(HolidayDAO dao) {
        this.dao = dao;
    }

    public boolean addHoliday(CompanyHoliday holiday) {
        return dao.addHoliday(holiday);
    }

    public List<CompanyHoliday> getHolidays() {
        return dao.getAllHolidays();
    }
}
