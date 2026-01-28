package com.revworkforce.controller;

import com.revworkforce.model.Announcement;
import com.revworkforce.model.CompanyHoliday;
import com.revworkforce.model.Employee;
import com.revworkforce.model.Goal;
import com.revworkforce.model.Leave;
import com.revworkforce.model.Notification;
import com.revworkforce.model.PerformanceReview;
import com.revworkforce.service.AnnouncementService;
import com.revworkforce.service.EmployeeService;
import com.revworkforce.service.GoalService;
import com.revworkforce.service.HolidayService;
import com.revworkforce.service.LeaveService;
import com.revworkforce.service.NotificationService;
import com.revworkforce.service.PerformanceService;
import com.revworkforce.util.InputValidator;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmployeeController {

    private static final EmployeeService empService = new EmployeeService();
    private static final LeaveService leaveService = new LeaveService();
    private static final GoalService goalService = new GoalService();
    private static final HolidayService holidayService = new HolidayService();
    private static final AnnouncementService announcementService = new AnnouncementService();
    private static final NotificationService notificationService = new NotificationService();
    private static final PerformanceService performanceService = new PerformanceService();

    public static void show(Employee emp) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("\n===== EMPLOYEE MENU =====");
                System.out.println("1. My Profile");
                System.out.println("2. Leave Management");
                System.out.println("3. Performance Goals");
                System.out.println("4. Holidays & Announcements");
                System.out.println("5. Notifications");
                System.out.println("0. Logout");
                System.out.print("Choose: ");

                if (!sc.hasNextInt()) {
                    System.out.println("Invalid choice.");
                    sc.next();
                    continue;
                }
                int ch = sc.nextInt();

                switch (ch) {
                    case 1 -> handleProfile(sc, emp);
                    case 2 -> handleLeave(sc, emp);
                    case 3 -> handlePerformance(sc, emp);
                    case 4 -> handleInfo();
                    case 5 -> handleNotifications(emp);
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Invalid Choice");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                sc.nextLine();
            }
        }
    }

    private static void handleProfile(Scanner sc, Employee emp) {
        System.out.println("\n--- My Profile ---");
        System.out.println("Name: " + emp.getName());
        System.out.println("Email: " + emp.getEmail());
        System.out.println("Phone: " + emp.getPhone());
        System.out.println("Address: " + emp.getAddress());
        System.out.println("Emerg. Contact: " + emp.getEmergencyContact());
        System.out.println("DOB: " + emp.getDob());

        Employee manager = empService.getEmployeeById(emp.getManagerId());
        if (manager != null) {
            System.out.println("Manager: " + manager.getName() + " (" + manager.getEmail() + ")");
        }

        System.out.println("1. Edit Profile  0. Back");
        int ch = sc.nextInt();
        if (ch == 1) {
            sc.nextLine();

            String ph;
            while (true) {
                System.out.print("New Phone (Enter to keep " + emp.getPhone() + "): ");
                ph = sc.nextLine();
                if (ph.isBlank()) {
                    ph = emp.getPhone();
                    break;
                }
                if (InputValidator.isValidPhone(ph))
                    break;
                System.out.println("Invalid phone format (10 digits).");
            }

            System.out.print("New Address: ");
            String ad = sc.nextLine();

            System.out.print("New Emergency Contact: ");
            String ec = sc.nextLine();

            if (empService.updateProfile(emp.getEmployeeId(), ph, ad, ec)) {
                System.out.println("Profile Updated");
                emp.setPhone(ph);
                emp.setAddress(ad);
                emp.setEmergencyContact(ec);
            } else {
                System.out.println("Failed");
            }
        }
    }

    private static void handleLeave(Scanner sc, Employee emp) {
        System.out.println("\n--- Leave Management ---");
        System.out.println("1. View Balance");
        System.out.println("2. Apply for Leave");
        System.out.println("3. View History");
        System.out.println("4. Cancel Pending Leave");
        System.out.print("Choice: ");
        int ch = sc.nextInt();

        try {
            switch (ch) {
                case 1 -> {
                    Map<String, Integer> balance = leaveService.getLeaveBalance(emp.getEmployeeId());
                    if (!balance.isEmpty()) {
                        System.out.printf("CL: %d | SL: %d | PL: %d%n",
                                balance.getOrDefault("CL", 0),
                                balance.getOrDefault("SL", 0),
                                balance.getOrDefault("PL", 0));
                    } else {
                        System.out.println("No balance record found.");
                    }
                }
                case 2 -> {
                    Leave lr = new Leave();
                    lr.setEmployeeId(emp.getEmployeeId());
                    sc.nextLine();
                    System.out.print("Type (CL/SL/PL): ");
                    lr.setLeaveType(sc.nextLine());

                    while (true) {
                        System.out.print("Start Date (YYYY-MM-DD): ");
                        String d = sc.next();
                        if (InputValidator.isValidDate(d)) {
                            lr.setStartDate(java.sql.Date.valueOf(d));
                            break;
                        }
                        System.out.println("Invalid date.");
                    }
                    while (true) {
                        System.out.print("End Date (YYYY-MM-DD): ");
                        String d = sc.next();
                        if (InputValidator.isValidDate(d)) {
                            lr.setEndDate(java.sql.Date.valueOf(d));
                            break;
                        }
                        System.out.println("Invalid date.");
                    }
                    sc.nextLine();
                    System.out.print("Reason: ");
                    lr.setReason(sc.nextLine());

                    System.out.println(leaveService.applyLeave(lr) ? "Applied" : "Failed");
                }
                case 3 -> {
                    List<Leave> list = leaveService.getLeavesByEmployee(emp.getEmployeeId());
                    System.out.println("Type | Start | End | Status | Comment");
                    for (Leave l : list) {
                        System.out.printf("%s | %s | %s | %s | %s%n",
                                l.getLeaveType(), l.getStartDate(), l.getEndDate(),
                                l.getStatus(), l.getManagerComment());
                    }
                }
                case 4 -> {
                    System.out.print("Enter Leave ID to Cancel: ");
                    int lid = sc.nextInt();
                    if (leaveService.cancelPendingLeave(lid, emp.getEmployeeId())) {
                        System.out.println("Leave Cancelled");
                    } else {
                        System.out.println("Failed (Check ID or if Status is PENDING)");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handlePerformance(Scanner sc, Employee emp) {
        System.out.println("\n1. Create Goal  2. View Goals  3. Update Progress  4. Submit Review  5. View Review");
        int ch = sc.nextInt();
        switch (ch) {
            case 1 -> createGoal(sc, emp);
            case 2 -> viewGoals(emp);
            case 3 -> updateProgress(sc);
            case 4 -> createReview(sc, emp);
            case 5 -> viewReview(emp);
        }
    }

    private static void viewReview(Employee emp) {
        try {
            List<PerformanceReview> list = performanceService.getEmployeeReview(emp.getEmployeeId());
            System.out.println("Year | Status | Manager Feedback | Rating");
            for (PerformanceReview pr : list) {
                System.out.printf("%d | %s | %s | %d%n",
                        pr.getYear(), pr.getStatus(),
                        pr.getManagerFeedback(), pr.getRating());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleInfo() {
        try {
            System.out.println("\n--- Holidays ---");
            List<CompanyHoliday> holidays = holidayService.getHolidays();
            for (CompanyHoliday h : holidays) {
                System.out.printf("%s : %s (%s)%n", h.getHolidayDate(), h.getName(), h.getType());
            }

            System.out.println("\n--- Announcements ---");
            List<Announcement> anns = announcementService.getAnnouncements();
            for (Announcement a : anns) {
                System.out.printf("[%s] %s%n", a.getPostedDate(), a.getMessage());
            }

            System.out.println("\n--- Upcoming Birthdays (30 Days) ---");
            List<Employee> bdays = empService.getUpcomingBirthdays();
            for (Employee e : bdays) {
                System.out.printf("%s : %s%n", e.getName(), e.getDob());
            }

            System.out.println("\n--- Work Anniversaries (Result Month) ---");
            List<Employee> annivs = empService.getWorkAnniversaries();
            for (Employee e : annivs) {
                System.out.printf("%s : %s%n", e.getName(), e.getJoiningDate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createGoal(Scanner sc, Employee emp) {

        Goal g = new Goal();
        g.setEmployeeId(emp.getEmployeeId());

        sc.nextLine();
        System.out.print("Goal Description: ");
        g.setGoalDescription(sc.nextLine());

        while (true) {
            System.out.print("Deadline (YYYY-MM-DD): ");
            String d = sc.next();
            if (InputValidator.isValidDate(d)) {
                g.setDeadline(java.sql.Date.valueOf(d));
                break;
            }
            System.out.println("Invalid date.");
        }

        sc.nextLine();
        System.out.print("Priority (HIGH/MEDIUM/LOW): ");
        g.setPriority(sc.nextLine());

        System.out.print("Success Metrics: ");
        g.setSuccessMetrics(sc.nextLine());

        System.out.println(
                goalService.createGoal(g)
                        ? "Goal Created"
                        : "Failed");
    }

    private static void viewGoals(Employee emp) {
        try {
            List<Goal> list = goalService.getEmployeeGoals(emp.getEmployeeId());

            System.out.println("ID | Goal | Deadline | Priority | Progress | Status");
            for (Goal g : list) {
                System.out.printf("%d | %s | %s | %s | %d%% | %s%n",
                        g.getGoalId(),
                        g.getGoalDescription(),
                        g.getDeadline(),
                        g.getPriority(),
                        g.getProgress(),
                        g.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateProgress(Scanner sc) {

        System.out.print("Goal ID: ");
        int id = sc.nextInt();

        System.out.print("Progress (0–100): ");
        int p = sc.nextInt();

        System.out.println(
                goalService.updateProgress(id, p)
                        ? "✅ Updated"
                        : "❌ Failed");
    }

    private static void createReview(Scanner sc, Employee emp) {
        PerformanceReview pr = new PerformanceReview();
        pr.setEmployeeId(emp.getEmployeeId());

        sc.nextLine();
        System.out.print("Year (YYYY): ");
        pr.setYear(sc.nextInt());
        sc.nextLine();
        System.out.print("Self-Assessment: ");
        pr.setSelfAssessment(sc.nextLine());
        System.out.print("Achievements: ");
        pr.setAchievements(sc.nextLine());
        System.out.print("Areas of Improvement: ");
        pr.setImprovementAreas(sc.nextLine());

        System.out.println(performanceService.submitReview(pr) ? "Submitted" : "Failed");
    }

    private static void handleNotifications(Employee emp) {
        System.out.println("\n--- Notifications ---");
        try {
            List<Notification> list = notificationService.getNotifications(emp.getEmployeeId());
            if (list.isEmpty()) {
                System.out.println("No notifications.");
            } else {
                for (Notification n : list) {
                    String status = n.isRead() ? "[Read]" : "[NEW]";
                    System.out.printf("%s %s (%s)%n", status, n.getMessage(), n.getCreatedAt());
                }
                notificationService.markAsRead(emp.getEmployeeId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
