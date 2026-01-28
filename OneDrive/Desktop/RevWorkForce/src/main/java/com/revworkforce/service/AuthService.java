package com.revworkforce.service;

import com.revworkforce.dao.EmployeeDAO;
import com.revworkforce.model.Employee;
import com.revworkforce.util.PasswordUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private EmployeeDAO employeeDAO;

    // Default constructor for normal app usage
    public AuthService() {
        this.employeeDAO = new EmployeeDAO();
    }

    // Constructor for testing
    public AuthService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public Employee authenticate(int empId, String password) {
        logger.info("Attempting authentication for user ID: {}", empId);
        String hashedPassword = PasswordUtil.hashPassword(password);
        return employeeDAO.login(empId, hashedPassword);
    }

    public String register(Employee emp) {
        logger.info("Attempting to register new employee with ID: {}", emp.getEmployeeId());
        if ("ADMIN".equalsIgnoreCase(emp.getRole())) {
            int adminCount = employeeDAO.getAdminCount();
            if (adminCount >= 4) {
                return "ADMIN_LIMIT_REACHED";
            }
        }
        if (employeeDAO.getEmployeeById(emp.getEmployeeId()) != null) {
            return "ID_ALREADY_EXISTS";
        }

        // Hash password before saving
        emp.setPassword(PasswordUtil.hashPassword(emp.getPassword()));

        // Hash security answer
        if (emp.getSecurityAnswer() != null) {
            emp.setSecurityAnswer(PasswordUtil.hashPassword(emp.getSecurityAnswer()));
        }

        return employeeDAO.addEmployee(emp) ? "SUCCESS" : "DB_ERROR";
    }

    public String[] getSecurityInfo(int empId) {
        return employeeDAO.getSecurityInfo(empId);
    }

    public boolean resetPassword(int empId, String newPassword) {
        logger.info("Password reset requested for user ID: {}", empId);
        return employeeDAO.resetPassword(empId, PasswordUtil.hashPassword(newPassword));
    }
}
