package com.revworkforce.controller;

import com.revworkforce.model.Announcement;
import com.revworkforce.model.CompanyHoliday;
import com.revworkforce.model.Employee;
import com.revworkforce.service.AnnouncementService;
import com.revworkforce.service.EmployeeService;
import com.revworkforce.service.HolidayService;
import com.revworkforce.service.LeaveService;
import com.revworkforce.service.AuditLogService;
import com.revworkforce.util.InputValidator;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminController {

    private static final EmployeeService empService = new EmployeeService();
    private static final HolidayService holidayService = new HolidayService();
    private static final AnnouncementService announcementService = new AnnouncementService();
    private static final LeaveService leaveService = new LeaveService();
    private static final AuditLogService auditLogService = new AuditLogService();

    public static void show(Employee admin) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("\n===== ADMIN MENU =====");
                System.out.println("1. Add Employee");
                System.out.println("2. View All Employees");
                System.out.println("3. Update Employee");
                System.out.println("4. Deactivate Employee");
                System.out.println("5. System Configuration");
                System.out.println("6. Search Employees");
                System.out.println("7. View Audit Logs");
                System.out.println("8. Reset Password");
                System.out.println("9. Adjust Leave Balance");
                System.out.println("10. Revoke Leave");
                System.out.println("11. Generate Leave Report");
                System.out.println("0. Logout");

                System.out.print("Choose: ");
                if (!sc.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a number.");
                    sc.next();
                    continue;
                }
                int choice = sc.nextInt();

                switch (choice) {
                    case 1 -> addEmployee(sc, admin);
                    case 2 -> viewEmployees();
                    case 3 -> updateEmployeeDetails(sc, admin);
                    case 4 -> deactivateEmployee(sc, admin);
                    case 5 -> handleSystemConfig(sc, admin);
                    case 6 -> searchEmployees(sc);
                    case 7 -> viewAuditLogs();
                    case 8 -> resetPassword(sc, admin);
                    case 9 -> adjustLeaveBalance(sc, admin);
                    case 10 -> revokeLeave(sc, admin);
                    case 11 -> generateLeaveReport();
                    case 0 -> {
                        System.out.println("Logged out");
                        return;
                    }
                    default -> System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                sc.nextLine(); // Clear buffer
            }
        }
    }

    private static void addEmployee(Scanner sc, Employee admin) {
        Employee emp = new Employee();

        System.out.print("Employee ID: ");
        if (!sc.hasNextInt()) {
            System.out.println("ID must be a number.");
            sc.next();
            return;
        }
        emp.setEmployeeId(sc.nextInt());

        sc.nextLine();
        System.out.print("Name: ");
        emp.setName(sc.nextLine());

        while (true) {
            System.out.print("Email: ");
            String e = sc.nextLine();
            if (InputValidator.isValidEmail(e)) {
                emp.setEmail(e);
                break;
            }
            System.out.println("Invalid email format.");
        }

        System.out.print("Password: ");
        emp.setPassword(sc.nextLine());

        System.out.print("Role (EMPLOYEE/MANAGER): ");
        emp.setRole(sc.nextLine());

        System.out.print("Manager ID: ");
        emp.setManagerId(sc.nextInt());

        sc.nextLine();
        while (true) {
            System.out.print("Phone (10 digits): ");
            String p = sc.nextLine();
            if (InputValidator.isValidPhone(p)) {
                emp.setPhone(p);
                break;
            }
            System.out.println("Invalid phone number.");
        }

        System.out.print("Address: ");
        emp.setAddress(sc.nextLine());

        System.out.print("Department: ");
        emp.setDepartment(sc.nextLine());

        System.out.print("Designation: ");
        emp.setDesignation(sc.nextLine());

        while (true) {
            System.out.print("Joining Date (YYYY-MM-DD): ");
            String dateStr = sc.next();
            if (InputValidator.isValidDate(dateStr)) {
                emp.setJoiningDate(Date.valueOf(dateStr));
                break;
            }
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }

        System.out.print("Salary: ");
        if (sc.hasNextDouble()) {
            emp.setSalary(sc.nextDouble());
        } else {
            System.out.println("Invalid salary. Setting to 0.");
            sc.next();
            emp.setSalary(0);
        }

        boolean added = empService.addEmployee(emp);
        if (added) {
            System.out.println("Employee added");
            auditLogService.logAction("ADD_EMPLOYEE", admin.getEmployeeId(), "Added employee: " + emp.getEmployeeId());
        } else {
            System.out.println("Failed");
        }
    }

    private static void viewEmployees() {
        try {
            List<Employee> list = empService.getAllEmployees();
            System.out.println("\n-----------------------------------------------------------");
            System.out.printf("%-5s | %-20s | %-10s | %-10s | %-10s%n", "ID", "Name", "Role", "Dept", "Status");
            System.out.println("-----------------------------------------------------------");
            for (Employee e : list) {
                System.out.printf("%-5d | %-20s | %-10s | %-10s | %-10s%n",
                        e.getEmployeeId(),
                        truncate(e.getName(), 20),
                        e.getRole(),
                        truncate(e.getDepartment(), 10),
                        e.getStatus());
            }
            System.out.println("-----------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String truncate(String s, int len) {
        if (s == null)
            return "";
        return s.length() > len ? s.substring(0, len - 3) + "..." : s;
    }

    private static void deactivateEmployee(Scanner sc, Employee admin) {
        System.out.print("Employee ID to deactivate: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid ID.");
            sc.next();
            return;
        }
        int id = sc.nextInt();

        boolean ok = empService.deactivateEmployee(id);
        if (ok) {
            System.out.println("Deactivated");
            auditLogService.logAction("DEACTIVATE_EMPLOYEE", admin.getEmployeeId(), "Deactivated ID: " + id);
        } else {
            System.out.println("Failed");
        }
    }

    private static void updateEmployeeDetails(Scanner sc, Employee admin) {
        Employee emp = new Employee();
        System.out.print("ID of Employee to Update: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid ID.");
            sc.next();
            return;
        }
        emp.setEmployeeId(sc.nextInt());
        sc.nextLine();

        System.out.print("New Name: ");
        emp.setName(sc.nextLine());

        System.out.print("New Email (Enter to skip validation check if unchanged or valid): ");
        emp.setEmail(sc.nextLine()); // For update, we might allow non-strict logic or should enforce? Enforce is
                                     // safer.
        // Actually for simplicity in update, we just take input.

        System.out.print("New Role: ");
        emp.setRole(sc.nextLine());
        System.out.print("New Manager ID: ");
        if (sc.hasNextInt()) {
            emp.setManagerId(sc.nextInt());
        } else {
            sc.next(); // Skip invalid or assume 0
            emp.setManagerId(0);
        }
        sc.nextLine();
        System.out.print("New Dept: ");
        emp.setDepartment(sc.nextLine());
        System.out.print("New Designation: ");
        emp.setDesignation(sc.nextLine());
        System.out.print("New Salary: ");
        if (sc.hasNextDouble()) {
            emp.setSalary(sc.nextDouble());
        } else {
            sc.next();
            emp.setSalary(0);
        }

        boolean updated = empService.updateEmployee(emp);
        if (updated) {
            System.out.println("Updated");
            auditLogService.logAction("UPDATE_EMPLOYEE", admin.getEmployeeId(),
                    "Updated details for ID: " + emp.getEmployeeId());
        } else {
            System.out.println("Failed");
        }
    }

    private static void handleSystemConfig(Scanner sc, Employee admin) {
        System.out.println("\n1. Add Holiday  2. Add Announcement");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid Input");
            sc.next();
            return;
        }
        int ch = sc.nextInt();
        sc.nextLine();

        if (ch == 1) {
            CompanyHoliday h = new CompanyHoliday();
            while (true) {
                System.out.print("Date (YYYY-MM-DD): ");
                String d = sc.next();
                if (InputValidator.isValidDate(d)) {
                    h.setHolidayDate(Date.valueOf(d));
                    break;
                }
                System.out.println("Invalid Date.");
            }
            sc.nextLine();
            System.out.print("Name: ");
            h.setName(sc.nextLine());
            System.out.print("Type: ");
            h.setType(sc.nextLine());
            boolean ok = holidayService.addHoliday(h);
            if (ok) {
                System.out.println("Added");
                auditLogService.logAction("ADD_HOLIDAY", admin.getEmployeeId(), "Added Holiday: " + h.getName());
            } else {
                System.out.println("Failed");
            }
        } else if (ch == 2) {
            Announcement a = new Announcement();
            System.out.print("Message: ");
            a.setMessage(sc.nextLine());
            a.setPostedBy(admin.getEmployeeId());
            a.setPostedDate(new Date(System.currentTimeMillis())); // Today
            boolean ok = announcementService.addAnnouncement(a);
            if (ok) {
                System.out.println("Posted");
                auditLogService.logAction("ADD_ANNOUNCEMENT", admin.getEmployeeId(), "Posted Announcement");
            } else {
                System.out.println("Failed");
            }
        }
    }

    private static void searchEmployees(Scanner sc) {
        System.out.print("Enter Name, Dept, or Designation to search: ");
        sc.nextLine(); // consume newline
        String query = sc.nextLine();
        try {
            List<Employee> list = empService.searchEmployees(query);
            System.out.println("\n--- Search Results ---");
            System.out.printf("%-5s | %-15s | %-10s | %-10s | %-15s%n", "ID", "Name", "Role", "Dept", "Designation");

            if (list.isEmpty()) {
                System.out.println("No matches found.");
            } else {
                for (Employee e : list) {
                    System.out.printf("%-5d | %-15s | %-10s | %-10s | %-15s%n",
                            e.getEmployeeId(),
                            truncate(e.getName(), 15),
                            e.getRole(),
                            truncate(e.getDepartment(), 10),
                            truncate(e.getDesignation(), 15));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void viewAuditLogs() {
        try {
            List<Map<String, Object>> list = auditLogService.getSystemLogs();
            System.out.println("\n--- Audit Logs ---");
            System.out.printf("%-25s | %-10s | %-20s | %-30s%n", "Time", "Admin ID", "Action", "Details");
            for (Map<String, Object> log : list) {
                System.out.printf("%-25s | %-10s | %-20s | %-30s%n",
                        log.get("timestamp"),
                        log.get("performed_by"),
                        log.get("action"),
                        log.get("details"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void resetPassword(Scanner sc, Employee admin) {
        System.out.print("Enter Employee ID to reset password: ");
        if (!sc.hasNextInt())
            return;
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter New Password: ");
        String newPass = sc.nextLine();

        boolean ok = empService.resetPassword(id, newPass);
        if (ok) {
            System.out.println("Password Reset Successfully");
            auditLogService.logAction("RESET_PASSWORD", admin.getEmployeeId(), "Reset password for ID: " + id);
        } else {
            System.out.println("Failed");
        }
    }

    private static void adjustLeaveBalance(Scanner sc, Employee admin) {
        System.out.print("Enter Employee ID: ");
        int id = sc.nextInt();
        System.out.print("Enter New CL Balance: ");
        int cl = sc.nextInt();
        System.out.print("Enter New SL Balance: ");
        int sl = sc.nextInt();
        System.out.print("Enter New PL Balance: ");
        int pl = sc.nextInt();

        boolean ok = leaveService.updateLeaveBalance(id, cl, sl, pl);
        if (ok) {
            System.out.println("Balance Updated");
            auditLogService.logAction("UPDATE_LEAVE_BALANCE", admin.getEmployeeId(), "Updated balance for ID: " + id);
        } else {
            System.out.println("Failed");
        }
    }

    private static void revokeLeave(Scanner sc, Employee admin) {
        System.out.print("Enter Leave ID to Revoke: ");
        int lid = sc.nextInt();
        if (leaveService.revokeLeave(lid)) {
            System.out.println("Leave Revoked");
            auditLogService.logAction("REVOKE_LEAVE", admin.getEmployeeId(), "Revoked Leave ID: " + lid);
        } else {
            System.out.println("Failed");
        }
    }

    private static void generateLeaveReport() {
        try {
            List<Map<String, Object>> list = leaveService.getLeaveStatistics();
            System.out.println("\n--- Leave Statistics (Dept-wise) ---");
            System.out.printf("%-20s | %-10s | %-5s%n", "Dept", "Type", "Count");
            for (Map<String, Object> stat : list) {
                System.out.printf("%-20s | %-10s | %-5d%n",
                        stat.get("department"),
                        stat.get("leave_type"),
                        stat.get("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
