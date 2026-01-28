package com.revworkforce;

import com.revworkforce.controller.AuthController;

public class Main {
    public static void main(String[] args) {
        while (true) {
            if (!AuthController.show()) {
                System.out.println("Exiting Application. Goodbye!");
                break;
            }
        }
    }
}
