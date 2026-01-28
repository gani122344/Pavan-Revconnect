package com.revworkforce.dao;

import com.revworkforce.model.Employee;
import com.revworkforce.util.DBConnection;
import com.revworkforce.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public Employee login(int empId, String password) {
        String sql = """
                    SELECT employee_id, name, email, password, role, manager_id, phone, address, dob, emergency_contact
                    FROM employee
                    WHERE employee_id = ? AND status = 'ACTIVE'
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, empId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.checkPassword(password, storedHash)) {
                        Employee emp = new Employee();
                        emp.setEmployeeId(rs.getInt("employee_id"));
                        emp.setName(rs.getString("name"));
                        emp.setEmail(rs.getString("email"));
                        emp.setRole(rs.getString("role"));
                        emp.setManagerId(rs.getInt("manager_id"));
                        emp.setPhone(rs.getString("phone"));
                        emp.setAddress(rs.getString("address"));
                        emp.setDob(rs.getDate("dob"));
                        emp.setEmergencyContact(rs.getString("emergency_contact"));
                        return emp;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Invalid credentials or error
    }

    public boolean addEmployee(Employee emp) {
        String sql = """
                    INSERT INTO employee
                    (employee_id, name, email, password, role, manager_id,
                     phone, address, department, designation, joining_date, salary, dob, emergency_contact, status, security_question, security_answer)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, emp.getEmployeeId());
            ps.setString(2, emp.getName());
            ps.setString(3, emp.getEmail());
            // Hash password before storing
            ps.setString(4, PasswordUtil.hashPassword(emp.getPassword()));
            ps.setString(5, emp.getRole());
            ps.setInt(6, emp.getManagerId());
            ps.setString(7, emp.getPhone());
            ps.setString(8, emp.getAddress());
            ps.setString(9, emp.getDepartment());
            ps.setString(10, emp.getDesignation());
            ps.setDate(11, emp.getJoiningDate());
            ps.setDouble(12, emp.getSalary());
            ps.setDate(13, emp.getDob());
            ps.setString(14, emp.getEmergencyContact());
            ps.setString(15, emp.getSecurityQuestion());
            ps.setString(16, emp.getSecurityAnswer());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Initialize Leave Balance
                try (PreparedStatement ps2 = con
                        .prepareStatement("INSERT INTO leave_balance (employee_id) VALUES (?)")) {
                    ps2.setInt(1, emp.getEmployeeId());
                    ps2.executeUpdate();
                }
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deactivateEmployee(int empId) {
        String sql = "UPDATE employee SET status='INACTIVE' WHERE employee_id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get Employee by ID
    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employee WHERE employee_id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update Profile (Employee)
    public boolean updateProfile(int empId, String phone, String address, String emergencyContact) {
        String sql = "UPDATE employee SET phone=?, address=?, emergency_contact=? WHERE employee_id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, address);
            ps.setString(3, emergencyContact);
            ps.setInt(4, empId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update Employee (Admin)
    public boolean updateEmployee(Employee emp) {
        String sql = """
                    UPDATE employee
                    SET name=?, email=?, role=?, manager_id=?, department=?, designation=?, salary=?
                    WHERE employee_id=?
                """;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emp.getName());
            ps.setString(2, emp.getEmail());
            ps.setString(3, emp.getRole());
            ps.setInt(4, emp.getManagerId());
            ps.setString(5, emp.getDepartment());
            ps.setString(6, emp.getDesignation());
            ps.setDouble(7, emp.getSalary());
            ps.setInt(8, emp.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Count Admins
    public int getAdminCount() {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM employee WHERE role='ADMIN'");
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employee";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Get Employees by Manager
    public List<Employee> getEmployeesByManager(int managerId) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employee WHERE manager_id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Upcoming Birthdays
    public List<Employee> getUpcomingBirthdays() {
        List<Employee> list = new ArrayList<>();
        String sql = """
                   SELECT name, dob FROM employee
                   WHERE DATE_ADD(dob, INTERVAL YEAR(CURDATE())-YEAR(dob) + IF(DAYOFYEAR(CURDATE()) > DAYOFYEAR(dob),1,0) YEAR)
                   BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
                """;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Employee e = new Employee();
                e.setName(rs.getString("name"));
                e.setDob(rs.getDate("dob"));
                list.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Work Anniversaries
    public List<Employee> getWorkAnniversaries() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT name, joining_date FROM employee WHERE MONTH(joining_date) = MONTH(CURDATE())";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Employee e = new Employee();
                e.setName(rs.getString("name"));
                e.setJoiningDate(rs.getDate("joining_date"));
                list.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Search Employees
    public List<Employee> searchEmployees(String query) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employee WHERE name LIKE ? OR department LIKE ? OR designation LIKE ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Reset Password
    public boolean resetPassword(int empId, String newPassword) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE employee SET password=? WHERE employee_id=?")) {
            ps.setString(1, PasswordUtil.hashPassword(newPassword));
            ps.setInt(2, empId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get Security QA for Forgot Password
    public String[] getSecurityInfo(int empId) {
        String sql = "SELECT security_question, security_answer FROM employee WHERE employee_id=?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[] { rs.getString("security_question"), rs.getString("security_answer") };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // ID not found or error
    }

    // Mapper helper
    private Employee mapRow(ResultSet rs) throws java.sql.SQLException {
        Employee emp = new Employee();
        emp.setEmployeeId(rs.getInt("employee_id"));
        emp.setName(rs.getString("name"));
        emp.setEmail(rs.getString("email"));
        emp.setRole(rs.getString("role"));
        emp.setManagerId(rs.getInt("manager_id"));
        emp.setPhone(rs.getString("phone"));
        emp.setAddress(rs.getString("address"));
        try {
            emp.setDepartment(rs.getString("department"));
        } catch (Exception e) {
        }
        try {
            emp.setDesignation(rs.getString("designation"));
        } catch (Exception e) {
        }
        try {
            emp.setJoiningDate(rs.getDate("joining_date"));
        } catch (Exception e) {
        }
        try {
            emp.setSalary(rs.getDouble("salary"));
        } catch (Exception e) {
        }
        try {
            emp.setStatus(rs.getString("status"));
        } catch (Exception e) {
        }
        try {
            emp.setDob(rs.getDate("dob"));
        } catch (Exception e) {
        }
        try {
            emp.setEmergencyContact(rs.getString("emergency_contact"));
        } catch (Exception e) {
        }
        return emp;
    }

}
