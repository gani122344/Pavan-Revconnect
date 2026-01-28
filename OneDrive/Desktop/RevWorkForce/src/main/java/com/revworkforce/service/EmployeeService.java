package com.revworkforce.service;

import com.revworkforce.dao.EmployeeDAO;
import com.revworkforce.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class EmployeeService {

    private static final Logger logger = LogManager.getLogger(EmployeeService.class);
    private final EmployeeDAO dao;

    public EmployeeService() {
        this.dao = new EmployeeDAO();
    }

    public EmployeeService(EmployeeDAO dao) {
        this.dao = dao;
    }

    public boolean addEmployee(Employee emp) {
        logger.info("Adding new employee: {}", emp.getEmployeeId());
        if (dao.getEmployeeById(emp.getEmployeeId()) != null) {
            logger.warn("Employee ID {} already exists!", emp.getEmployeeId());

            return false;
        }
        return dao.addEmployee(emp);
    }

    public List<Employee> getAllEmployees() {
        logger.info("Fetching all employees");
        return dao.getAllEmployees();
    }

    public boolean deactivateEmployee(int empId) {
        logger.info("Deactivating employee ID: {}", empId);
        return dao.deactivateEmployee(empId);
    }

    public boolean updateProfile(int empId, String phone, String address, String emergencyContact) {
        logger.info("Updating profile for employee ID: {}", empId);
        return dao.updateProfile(empId, phone, address, emergencyContact);
    }

    public boolean updateEmployee(Employee emp) {
        return dao.updateEmployee(emp);
    }

    public List<Employee> getEmployeesByManager(int managerId) {
        return dao.getEmployeesByManager(managerId);
    }

    public List<Employee> getUpcomingBirthdays() {
        return dao.getUpcomingBirthdays();
    }

    public List<Employee> getWorkAnniversaries() {
        return dao.getWorkAnniversaries();
    }

    public Employee getEmployeeById(int id) {
        return dao.getEmployeeById(id);
    }

    public List<Employee> searchEmployees(String query) {
        return dao.searchEmployees(query);
    }

    public boolean resetPassword(int empId, String newPassword) {
        return dao.resetPassword(empId, newPassword);
    }
}
