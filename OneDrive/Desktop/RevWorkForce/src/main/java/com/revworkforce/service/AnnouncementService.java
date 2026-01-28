package com.revworkforce.service;

import com.revworkforce.dao.AnnouncementDAO;
import com.revworkforce.model.Announcement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AnnouncementService {
    private static final Logger logger = LogManager.getLogger(AnnouncementService.class);
    private AnnouncementDAO dao;

    public AnnouncementService() {
        this.dao = new AnnouncementDAO();
    }

    public AnnouncementService(AnnouncementDAO dao) {
        this.dao = dao;
    }

    public boolean addAnnouncement(Announcement ann) {
        logger.info("Adding new announcement: {}", ann.getMessage());
        return dao.addAnnouncement(ann);
    }

    public List<Announcement> getAnnouncements() {
        logger.info("Fetching all announcements");
        return dao.getAllAnnouncements();
    }
}
