package org.example;

import org.example.controller.UserController;
import org.example.model.User;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        UserController controller = new UserController();
        Scanner sc= new Scanner(System.in);

        controller.register();
        User user = controller.login();

        if (user == null) {
            System.out.println("Login failed. Exiting...");
            return;
        }

        int choice;

        do {
            System.out.println("\n==== USER MENU ====");
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Change Password");
            System.out.println("4. Reset Password");
            System.out.println("5. Delete Account");
            System.out.println("6. Logout");
            System.out.print("Enter choice: ");

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    controller.viewProfile(user);
                    break;

                case 2:
                    controller.updateProfile(user);
                    break;

                case 3:
                    controller.changePassword(user);
                    break;

                case 4:
                    controller.resetPassword();
                    break;

                case 5:
                    controller.deleteAccount(user);
                    System.out.println("Account deleted. Logging out...");
                    return;

                case 6:
                    System.out.println("Logged out successfully.");
                    break;

                default:
                    System.out.println("Invalid choice");
            }

        } while (choice != 6);
    }
}
