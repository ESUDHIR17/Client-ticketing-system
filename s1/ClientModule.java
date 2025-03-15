package s1;

import java.sql.*;
import java.util.Scanner;

public class ClientModule {
    private Scanner scanner = new Scanner(System.in);
    private int clientId;

    public ClientModule(int clientId) {
        this.clientId = clientId;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n===== Client Menu =====");
            System.out.println("1. Create a Service Request");
            System.out.println("2. View My Requests");
            System.out.println("3. View Vendor Clarifications & Respond");
            System.out.println("4. Logout");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> createServiceRequest();
                case 2 -> viewRequests();
                case 3 -> viewVendorClarificationsAndRespond();
                case 4 -> {
                    System.out.println("Logging out... Returning to main menu.");
                    return; // Returns control to Main.java
                }
                default -> System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private void createServiceRequest() {

        System.out.print("Enter the title for the issue: ");
        String title = scanner.nextLine();

        System.out.print("Enter request description: ");
        String description = scanner.nextLine();

        System.out.println("Select the urgency of the issue:");
        System.out.println("1. Low");
        System.out.println("2. Medium");
        System.out.println("3. High");
        System.out.print("Enter your choice: ");
        int urgencyChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Map the choice to urgency level
        String urgency = switch (urgencyChoice) {
            case 1 -> "Low";
            case 2 -> "Medium";
            case 3 -> "High";
            default -> "Low"; // Default to Low if invalid input
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO tickets (client_id, title, description, urgency, status, created_date) VALUES (?, ?, ?, ?, 'Pending', NOW())";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, clientId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setString(4, urgency);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int ticketId = rs.getInt(1);
                System.out.println("Service request created successfully! Ticket ID: " + ticketId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewRequests() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT ticket_id, description, status FROM tickets WHERE client_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== Your Service Requests =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Ticket ID: " + rs.getInt("ticket_id"));
                System.out.println("Description: " + rs.getString("description"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("----------------------");
            }

            if (!found) System.out.println("No service requests found.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewVendorClarificationsAndRespond() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT c.clarification_id, c.ticket_id, c.question 
                FROM clarifications c
                JOIN tickets t ON c.ticket_id = t.ticket_id
                WHERE t.client_id = ? AND c.answer IS NULL
            """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== Vendor Clarifications =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int clarificationId = rs.getInt("clarification_id");
                int ticketId = rs.getInt("ticket_id");
                String question = rs.getString("question");

                System.out.println("Clarification ID: " + clarificationId);
                System.out.println("Ticket ID: " + ticketId);
                System.out.println("Vendor Question: " + question);
                System.out.print("Enter your response (leave empty to skip): ");

                String answer = scanner.nextLine();
                if (!answer.isEmpty()) {
                    respondToClarification(clarificationId, answer);
                }
            }

            if (!found) System.out.println("No clarifications pending your response.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void respondToClarification(int clarificationId, String answer) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE clarifications SET answer = ? WHERE clarification_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, answer);
            stmt.setInt(2, clarificationId);
            stmt.executeUpdate();
            System.out.println("Response submitted successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
