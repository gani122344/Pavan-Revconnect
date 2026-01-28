package com.revworkforce.service;

import com.revworkforce.dao.AnnouncementDAO;
import com.revworkforce.model.Announcement;

import java.util.List;

public class AnnouncementService {
    private AnnouncementDAO dao;

    public AnnouncementService() {
        this.dao = new AnnouncementDAO();
    }

    public AnnouncementService(AnnouncementDAO dao) {
        this.dao = dao;
    }

    public boolean addAnnouncement(Announcement ann) {
        return dao.addAnnouncement(ann);
    }

    public List<Announcement> getAnnouncements() {
        return dao.getAllAnnouncements();
    }
}
