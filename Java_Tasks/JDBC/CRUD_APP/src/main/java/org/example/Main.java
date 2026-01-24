package Java.JDBC.CRUD_APP.src.main.java.org.example;

import java.util.Scanner;

import Java.JDBC.CRUD_APP.src.main.java.org.example.dao.UserDAO;
import Java.JDBC.CRUD_APP.src.main.java.org.example.model.User;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserDAO dao = new UserDAO();
        User loggedUser = null;
        System.out.println("Hlooo Welcome to JDBC Console Application.");

        while (true) {

            System.out.println("1.Register 2.Login 3.Forgot Password 4.Exit");
            int command = sc.nextInt();
            sc.nextLine();

            if (command == 1) {
                System.out.print("Username: ");
                String uname = sc.nextLine();
                System.out.print("Password: ");
                String password = sc.nextLine();
                System.out.print("Email: ");
                String email = sc.nextLine();

                dao.registerUser(new User(uname, password, email));
                System.out.println("Registered!");

            } else if (command == 2) {

                System.out.print("Username: ");
                String username = sc.nextLine();
                System.out.print("Password: ");
                String password = sc.nextLine();

                loggedUser = dao.login(username, password);
                if (loggedUser == null) {
                    System.out.println("Invalid login / Account is inactive");
                    continue;
                }

                while (true) {
                    System.out.println("1.View 2.Update 3.ChangePass 4.Deactivate 5.Delete 6.Logout");
                    int operation = sc.nextInt();
                    sc.nextLine();

                    if (operation == 1) {
                        User u1 = dao.getUserById(loggedUser.getId());
                        System.out.println("User Name: " + u1.getUsername());
                        System.out.println("User ID: " + u1.getId());
                        System.out.println("User Email: " + u1.getEmail());
                        System.out.println("User Status: " + u1.getStatus());

                    } else if (operation == 2) {
                        System.out.print("New Username: ");
                        String new_user = sc.nextLine();
                        System.out.print("New Email: ");
                        String new_email = sc.nextLine();
                        dao.updateProfile(loggedUser.getId(), new_user, new_email);
                        System.out.println("Profile updated");

                    } else if (operation == 3) {
                        System.out.print("Old Password: ");
                        String old_password = sc.nextLine();
                        System.out.print("New Password: ");
                        String new_password = sc.nextLine();
                        System.out.println(
                                dao.changePassword(loggedUser.getId(), old_password, new_password) ? "Password changed"
                                        : "Wrong password");

                    } else if (operation == 4) {
                        dao.updateStatus(loggedUser.getId(), "INACTIVE");
                        System.out.println("Account deactivated");
                        break;

                    } else if (operation == 5) {
                        dao.deleteUser(loggedUser.getId());
                        System.out.println("Account deleted");
                        break;

                    } else if (operation == 6)
                        break;
                }

            } else if (command == 3) {
                System.out.print("Username: ");
                String user_name = sc.nextLine();
                System.out.print("New Password: ");
                String new_password = sc.nextLine();
                System.out.println(dao.resetPassword(user_name, new_password) ? "Password reset" : "User not found");

            } else {
                System.out.println("Terminating...");
                break;
            }
        }
        sc.close();
    }
}