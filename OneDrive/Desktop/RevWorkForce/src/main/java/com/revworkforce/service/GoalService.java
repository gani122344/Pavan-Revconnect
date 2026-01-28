package com.revworkforce.service;

import com.revworkforce.dao.GoalDAO;
import com.revworkforce.model.Goal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class GoalService {
    private static final Logger logger = LogManager.getLogger(GoalService.class);
    private GoalDAO dao;

    public GoalService() {
        this.dao = new GoalDAO();
    }

    public GoalService(GoalDAO dao) {
        this.dao = dao;
    }

    public boolean createGoal(Goal g) {
        logger.info("Creating new goal: {}", g.getGoalDescription());
        return dao.createGoal(g);
    }

    public List<Goal> getEmployeeGoals(int empId) {
        logger.info("Fetching goals for employee ID: {}", empId);
        return dao.getGoalsByEmployee(empId);
    }

    public boolean updateProgress(int goalId, int progress) {
        logger.info("Updating progress for goal ID: {} to {}%", goalId, progress);
        return dao.updateProgress(goalId, progress);
    }

    public List<Map<String, Object>> getTeamGoals(int managerId) {
        return dao.getTeamGoals(managerId);
    }
}
