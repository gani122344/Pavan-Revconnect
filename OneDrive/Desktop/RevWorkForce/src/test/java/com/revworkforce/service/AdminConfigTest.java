package com.revworkforce.service;

import com.revworkforce.dao.AnnouncementDAO;
import com.revworkforce.dao.HolidayDAO;
import com.revworkforce.model.Announcement;
import com.revworkforce.model.CompanyHoliday;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AdminConfigTest {

    @Mock
    private HolidayDAO holidayDAO;

    @Mock
    private AnnouncementDAO announcementDAO;

    @Test
    public void testAddHoliday() {
        HolidayService service = new HolidayService(holidayDAO);
        CompanyHoliday h = new CompanyHoliday();
        h.setName("New Year");

        Mockito.when(holidayDAO.addHoliday(h)).thenReturn(true);
        Assertions.assertTrue(service.addHoliday(h));
    }

    @Test
    public void testGetHolidays() {
        HolidayService service = new HolidayService(holidayDAO);
        List<CompanyHoliday> mockList = new ArrayList<>();
        CompanyHoliday h = new CompanyHoliday();
        h.setName("Diwali");
        mockList.add(h);

        Mockito.when(holidayDAO.getAllHolidays()).thenReturn(mockList);

        List<CompanyHoliday> result = service.getHolidays();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Diwali", result.get(0).getName());
    }

    @Test
    public void testAddAnnouncement() {
        AnnouncementService service = new AnnouncementService(announcementDAO);
        Announcement a = new Announcement();
        a.setMessage("Hello");

        Mockito.when(announcementDAO.addAnnouncement(a)).thenReturn(true);
        Assertions.assertTrue(service.addAnnouncement(a));
    }
}
