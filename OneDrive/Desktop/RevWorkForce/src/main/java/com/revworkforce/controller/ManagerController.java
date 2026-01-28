package com.revworkforce.controller;

import com.revworkforce.model.Employee;
import com.revworkforce.model.Leave;
import com.revworkforce.model.PerformanceReview;
import com.revworkforce.service.EmployeeService;
import com.revworkforce.service.GoalService;
import com.revworkforce.service.LeaveService;
import com.revworkforce.service.PerformanceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ManagerController {
    private static final Logger logger = LogManager.getLogger(ManagerController.class);

    private static final EmployeeService empService = new EmployeeService();
    private static final LeaveService leaveService = new LeaveService();
    private static final GoalService goalService = new GoalService();
    private static final PerformanceService performanceService = new PerformanceService();

    public static void show(Employee manager) {
        logger.info("Showing Manager Menu for managerId: {}", manager.getEmployeeId());
        Scanner sc = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("\n===== MANAGER MENU =====");
                System.out.println("1. Team Management (Show Balances)");
                System.out.println("2. Leave Applications");
                System.out.println("3. Performance Reviews");
                System.out.println("4. Team Goals");
                System.out.println("0. Logout");
                System.out.print("Choose: ");

                if (!sc.hasNextInt()) {
                    System.out.println("Invalid input.");
                    sc.next();
                    continue;
                }
                int ch = sc.nextInt();
                switch (ch) {
                    case 1 -> handleTeam(sc, manager);
                    case 2 -> handleLeaves(sc, manager);
                    case 3 -> handleReviews(sc, manager);
                    case 4 -> viewTeamGoals(manager);
                    case 0 -> {
                        logger.info("Manager logged out");
                        return;
                    }
                    default -> System.out.println("Invalid Choice");
                }
            } catch (Exception e) {
                logger.error("Error in ManagerController menu", e);
                System.out.println("Error: " + e.getMessage());
                sc.nextLine();
            }
        }
    }

    private static void handleTeam(Scanner sc, Employee manager) {
        System.out.println("\n1. List & Balances  2. View Member Profile  3. Attendance Summary");
        if (!sc.hasNextInt())
            return;
        int ch = sc.nextInt();
        if (ch == 1) {
            try {
                List<Map<String, Object>> list = leaveService.getTeamBalances(manager.getEmployeeId());
                System.out.println("\n--- Team Balances ---");
                System.out.printf("%-20s | %-5s | %-5s | %-5s%n", "Name", "CL", "SL", "PL");
                for (Map<String, Object> map : list) {
                    System.out.printf("%-20s | %-5d | %-5d | %-5d%n",
                            map.get("name"),
                            map.get("cl"),
                            map.get("sl"),
                            map.get("pl"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ch == 2) {
            System.out.print("Enter Employee ID: ");
            int eid = sc.nextInt();
            Employee e = empService.getEmployeeById(eid);
            // Verify he belongs towards this manager
            if (e != null && e.getManagerId() == manager.getEmployeeId()) {
                System.out.println("--- Profile ---");
                System.out.println("Name: " + e.getName());
                System.out.println("Email: " + e.getEmail());
                System.out.println("Phone: " + e.getPhone());
                System.out.println("Address: " + e.getAddress());
                System.out.println("Join Date: " + e.getJoiningDate());
                System.out.println("Designation: " + e.getDesignation());
                System.out.println("Salary: " + e.getSalary());
            } else {
                System.out.println("Employee not found or not in your team.");
            }
        } else if (ch == 3) {
            System.out.println("\n--- Today's Attendance ---");
            try {
                List<Map<String, Object>> list = leaveService.getTeamAttendance(manager.getEmployeeId());
                System.out.printf("%-5s | %-20s | %-20s%n", "ID", "Name", "Status");
                for (Map<String, Object> map : list) {
                    String status = (String) map.get("status");
                    if ("ON LEAVE".equals(status)) {
                        status += " (" + map.get("leave_type") + ")";
                    }
                    System.out.printf("%-5d | %-20s | %-20s%n",
                            map.get("employee_id"),
                            map.get("name"),
                            status);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleLeaves(Scanner sc, Employee manager) {
        System.out.println("\n1. View Pending Leaves  2. Approve/Reject  3. View Team Leave Calendar");
        if (!sc.hasNextInt())
            return;
        int ch = sc.nextInt();
        if (ch == 1) {
            try {
                List<Leave> list = leaveService.getPendingLeaves(manager.getEmployeeId());
                System.out.println("ID | Emp | Type | Start | End | Reason");
                for (Leave l : list) {
                    System.out.printf("%d | %s | %s | %s | %s | %s%n",
                            l.getLeaveId(), l.getEmployeeName(), l.getLeaveType(),
                            l.getStartDate(), l.getEndDate(), l.getReason());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ch == 2) {
            System.out.print("Leave ID: ");
            int lid = sc.nextInt();
            sc.nextLine();
            System.out.print("Status (APPROVED/REJECTED): ");
            String st = sc.nextLine();
            System.out.print("Comment: ");
            String cmt = sc.nextLine();
            System.out.println(leaveService.approveOrRejectLeave(lid, st, cmt) ? "Processed" : "Failed");
        } else if (ch == 3) {
            // Team Leave Calendar (Approved Leaves)
            try {
                System.out.println("\n--- Team Leave Calendar (Approved Only) ---");
                List<Leave> list = leaveService.getApprovedLeaves(manager.getEmployeeId());
                System.out.println("Emp Name | Type | Start Date | End Date");
                if (list.isEmpty()) {
                    System.out.println("No approved leaves found for team.");
                } else {
                    for (Leave l : list) {
                        System.out.printf("%s | %s | %s | %s%n",
                                l.getEmployeeName(), l.getLeaveType(),
                                l.getStartDate(), l.getEndDate());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleReviews(Scanner sc, Employee manager) {
        System.out.println("\n1. View Team Reviews  2. Give Feedback  3. Performance Summary");
        int ch = sc.nextInt();
        if (ch == 1) {
            try {
                List<PerformanceReview> list = performanceService.getTeamReviews(manager.getEmployeeId());
                System.out.println("RevID | Emp | Year | Status");
                for (PerformanceReview pr : list) {
                    System.out.printf("%d | %s | %d | %s%n",
                            pr.getReviewId(), pr.getEmployeeName(),
                            pr.getYear(), pr.getStatus());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ch == 2) {
            System.out.print("Review ID: ");
            int rid = sc.nextInt();
            sc.nextLine();
            System.out.print("Feedback: ");
            String fb = sc.nextLine();
            System.out.print("Rating (1-5): ");
            int rate = sc.nextInt();
            System.out.println(performanceService.giveFeedback(rid, fb, rate) ? "Submitted" : "Failed");
        } else if (ch == 3) {
            System.out.println("\n--- Performance Summary ---");
            try {
                List<PerformanceReview> list = performanceService.getTeamReviews(manager.getEmployeeId());
                System.out.println("Name | Rating | Status");
                for (PerformanceReview pr : list) {
                    int rating = pr.getRating();
                    String stars = "*".repeat(Math.max(0, rating));
                    System.out.printf("%s | %s (%d) | %s%n",
                            pr.getEmployeeName(), stars, rating, pr.getStatus());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void viewTeamGoals(Employee manager) {
        try {
            List<Map<String, Object>> list = goalService.getTeamGoals(manager.getEmployeeId());

            System.out.println("GoalID | Employee | Goal | Progress | Status");
            for (Map<String, Object> map : list) {
                System.out.printf("%d | %s | %s | %d%% | %s%n",
                        map.get("goal_id"),
                        map.get("name"),
                        map.get("goal_description"),
                        map.get("progress"),
                        map.get("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
