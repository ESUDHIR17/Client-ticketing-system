package s1;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== Customer Service Request System =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> registerUser();
                case 2 -> loginUser();
                case 3 -> {
                    System.out.println("Exiting system. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void registerUser() {
        System.out.println("\n===== User Registration =====");
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Select role (1: Client, 2: Vendor): ");
        int roleChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String role = (roleChoice == 1) ? "client" : "vendor";

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (name, username, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, role);
            stmt.executeUpdate();

            System.out.println("Registration successful! You can now log in.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loginUser() {
        System.out.println("\n===== User Login =====");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT user_id, role FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");

                System.out.println("Login successful! Redirecting to " + role + " module...");

                if (role.equals("client")) {
                    new ClientModule(userId).showMenu();
                } else if (role.equals("vendor")) {
                    new VendorModule(userId).showMenu();
                }

            } else {
                System.out.println("Invalid username or password. Try again.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
