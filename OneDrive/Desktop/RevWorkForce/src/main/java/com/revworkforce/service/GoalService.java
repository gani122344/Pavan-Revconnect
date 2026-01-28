package com.revworkforce.service;

import com.revworkforce.dao.GoalDAO;
import com.revworkforce.model.Goal;

import java.util.List;
import java.util.Map;

public class GoalService {
    private GoalDAO dao;

    public GoalService() {
        this.dao = new GoalDAO();
    }

    public GoalService(GoalDAO dao) {
        this.dao = dao;
    }

    public boolean createGoal(Goal g) {
        return dao.createGoal(g);
    }

    public List<Goal> getEmployeeGoals(int empId) {
        return dao.getGoalsByEmployee(empId);
    }

    public boolean updateProgress(int goalId, int progress) {
        return dao.updateProgress(goalId, progress);
    }

    public List<Map<String, Object>> getTeamGoals(int managerId) {
        return dao.getTeamGoals(managerId);
    }
}
