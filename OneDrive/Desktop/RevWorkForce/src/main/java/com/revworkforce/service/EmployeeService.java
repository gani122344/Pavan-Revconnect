package com.revworkforce.service;

import com.revworkforce.dao.EmployeeDAO;
import com.revworkforce.model.Employee;

import java.util.List;

public class EmployeeService {

    private final EmployeeDAO dao;

    public EmployeeService() {
        this.dao = new EmployeeDAO();
    }

    public EmployeeService(EmployeeDAO dao) {
        this.dao = dao;
    }

    public boolean addEmployee(Employee emp) {
        if (dao.getEmployeeById(emp.getEmployeeId()) != null) {
            System.out.println("Error: Employee ID " + emp.getEmployeeId() + " already exists!");
            return false;
        }
        return dao.addEmployee(emp);
    }

    public List<Employee> getAllEmployees() {
        return dao.getAllEmployees();
    }

    public boolean deactivateEmployee(int empId) {
        return dao.deactivateEmployee(empId);
    }

    public boolean updateProfile(int empId, String phone, String address, String emergencyContact) {
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
