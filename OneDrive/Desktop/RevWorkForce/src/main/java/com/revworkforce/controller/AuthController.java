package com.revworkforce.controller;

import com.revworkforce.model.Employee;
import com.revworkforce.model.Notification;
import com.revworkforce.service.AuthService;
import com.revworkforce.service.NotificationService;
import com.revworkforce.util.InputValidator;
import com.revworkforce.util.PasswordUtil;

import java.util.List;
import java.util.Scanner;

public class AuthController {

    public static boolean show() {
        Scanner sc = new Scanner(System.in);
        AuthService authService = new AuthService();
        NotificationService notificationService = new NotificationService();

        try {
            System.out.println("\n===== RevWorkForce =====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Forgot Password");
            System.out.println("0. Exit Application");
            System.out.print("Enter Choice: ");

            String choiceStr = sc.next();
            if (!choiceStr.matches("\\d+")) {
                System.out.println("Invalid input. Please enter a number.");
                return true;
            }
            int choice = Integer.parseInt(choiceStr);

            if (choice == 0) {
                return false; // Exit Application
            } else if (choice == 1) {
                System.out.print("Employee ID: ");
                if (!sc.hasNextInt()) {
                    System.out.println("ID must be a number.");
                    sc.next();
                    return true;
                }
                int empId = sc.nextInt();
                System.out.print("Password: ");
                String password = sc.next();

                Employee emp = authService.authenticate(empId, password);

                if (emp != null) {
                    processLogin(emp, notificationService);
                } else {
                    System.out.println("WRONG CREDENTIALS! Please check ID & Pass.");
                }
            } else if (choice == 2) {
                handleRegister(sc, authService);
            } else if (choice == 3) {
                handleForgot(authService, sc);
            } else {
                System.out.println("Invalid Option. Try again.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            sc.nextLine(); // Clear buffer
        }
        return true; // Loop back
    }

    private static void handleForgot(AuthService authService, Scanner sc) {
        System.out.println("\n--- Recover Password ---");
        System.out.print("Enter Employee ID: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid ID format.");
            sc.next();
            return;
        }
        int empId = sc.nextInt();
        sc.nextLine(); // consume newline

        String[] secInfo = authService.getSecurityInfo(empId);
        if (secInfo != null) {
            String question = secInfo[0];
            String storedHash = secInfo[1];

            if (question == null || storedHash == null) {
                System.out.println("Security Questions not set for this account.");
                return;
            }

            System.out.println("Security Question: " + question);
            System.out.print("Your Answer: ");
            String answer = sc.nextLine();

            // Check Answer: Answer is NOT double hashed in DB?
            // Actually, we don't know if security answer is hashed.
            // The previous logic was: PasswordUtil.hashPassword(answer).equals(storedHash)
            // Assuming the security answer is stored hashed in DB.
            // Let's keep it consistent.
            if (PasswordUtil.checkPassword(answer, storedHash)
                    || PasswordUtil.hashPassword(answer).equals(storedHash)) {
                // CheckPassword rehashes plain and compares.
                // The old code assumed `storedHash` IS the hash.
                // Wait, `checkPassword` does `hash(plain).equals(stored)`.
                // So `PasswordUtil.checkPassword(answer, storedHash)` is correct method.

                System.out.println("Identity Verified.");
                System.out.print("Enter New Password: ");
                String newPass = sc.nextLine();
                if (authService.resetPassword(empId, newPass)) {
                    System.out.println("Password Reset Successfully! Please login.");
                } else {
                    System.out.println("Failed to update password.");
                }
            } else {
                System.out.println("Incorrect Answer.");
            }

        } else {
            System.out.println("Employee ID not found.");
        }
    }

    private static void handleRegister(Scanner sc, AuthService authService) {
        System.out.println("\n===== Registration Form =====");
        System.out.println("Select Role to Register as: ");
        System.out.println("1. EMPLOYEE");
        System.out.println("2. MANAGER");
        System.out.println("3. ADMIN");
        System.out.print("Enter Choice: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid choice.");
            sc.next();
            return;
        }
        int roleChoice = sc.nextInt();
        sc.nextLine(); // consume newline

        String role = switch (roleChoice) {
            case 2 -> "MANAGER";
            case 3 -> "ADMIN";
            default -> "EMPLOYEE";
        };

        System.out.println("--- Enter Details for " + role + " ---");

        System.out.print("Enter ID: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid ID.");
            sc.next();
            return;
        }
        int empId = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        String email;
        while (true) {
            System.out.print("Enter Email: ");
            email = sc.nextLine();
            if (InputValidator.isValidEmail(email))
                break;
            System.out.println("Invalid email format. Try again.");
        }

        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        String phone;
        while (true) {
            System.out.print("Enter Phone (10 digits): ");
            phone = sc.nextLine();
            if (InputValidator.isValidPhone(phone))
                break;
            System.out.println("Invalid phone. Must be 10 digits.");
        }

        System.out.print("Enter Department: ");
        String dept = sc.nextLine();

        System.out.print("Security Question: ");
        String secQ = sc.nextLine();

        System.out.print("Security Answer: ");
        String secA = sc.nextLine();
        // We should hash the answer before storing if we want to.
        // But EmployeeDAO.addEmployee expects `security_answer` as a string.
        // It does NOT hash it inside addEmployee (only password).
        // So we should hash it HERE if we want it secured.
        // Or updated DAO.
        // Let's hash it here to be safe and consistent with `handleForgot`.
        String hashedSecA = PasswordUtil.hashPassword(secA);

        Employee emp = new Employee();
        emp.setEmployeeId(empId);
        emp.setName(name);
        emp.setEmail(email);
        emp.setPassword(password); // DAO will hash this
        emp.setPhone(phone);
        emp.setRole(role);
        emp.setDepartment(dept);
        emp.setJoiningDate(java.sql.Date.valueOf(java.time.LocalDate.now()));
        emp.setStatus("ACTIVE");
        emp.setAddress("Not Provided");
        emp.setDesignation("New Joinee");
        emp.setDob(java.sql.Date.valueOf("2000-01-01"));
        emp.setEmergencyContact("None");
        emp.setSecurityQuestion(secQ);
        emp.setSecurityAnswer(hashedSecA); // Storing hashed answer

        String result = authService.register(emp);

        if (result.equals("SUCCESS")) {
            System.out.println("Registration Successful! You can now Login.");
        } else if (result.equals("ADMIN_LIMIT_REACHED")) {
            System.out.println("Registration Failed: Admin limit (4) reached.");
        } else if (result.equals("ID_ALREADY_EXISTS")) {
            System.out.println("Registration Failed: ID " + empId + " already exists.");
        } else {
            System.out.println("Registration Failed: Database Error.");
        }
    }

    private static void processLogin(Employee emp, NotificationService notificationService) {
        int unread = 0;
        try {
            List<Notification> list = notificationService.getNotifications(emp.getEmployeeId());
            for (Notification n : list) {
                if (!n.isRead())
                    unread++;
            }
        } catch (Exception e) {
            System.out.println("Error fetching notifications.");
        }

        System.out.println("Welcome " + emp.getName() + " (" + emp.getRole() + ")");
        if (unread > 0)
            System.out.println("You have " + unread + " unread notifications!");

        switch (emp.getRole()) {
            case "EMPLOYEE" -> EmployeeController.show(emp);
            case "MANAGER" -> ManagerController.show(emp);
            case "ADMIN" -> AdminController.show(emp);
            default -> System.out.println("Unknown role");
        }
    }
}
