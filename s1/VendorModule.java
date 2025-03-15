package s1;

import java.sql.*;
import java.util.Scanner;

public class VendorModule {
    private Scanner scanner = new Scanner(System.in);
    private int vendorId;
    public VendorModule(int vendorId) {
        this.vendorId = vendorId;
    }
    public void showMenu() {
        while (true) {
            System.out.println("\n===== Vendor Menu =====");
            System.out.println("1. View Service Requests");
            System.out.println("2. View Pending Tickets Count");
            System.out.println("3. Ask for Clarification");
            System.out.println("4. View Client Responses");
            System.out.println("5. Resolve a Request");
            System.out.println("6. View Monthly Resolved Requests");
//            System.out.println("6. View Weekly Resolved Requests");

            System.out.println("7. Logout");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> viewServiceRequests();
                case 2 -> viewPendingRequestsCount();
                case 3 -> askForClarification();
                case 4 -> viewClientResponses();
                case 5 -> resolveRequest();
                case 6 -> viewMonthlyResolvedRequests();
//                case 6 -> viewWeeklyResolvedRequests();
                case 7 -> {
                    System.out.println("Logging out... Returning to main menu.");
                    return;
                }
                default -> System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private void viewServiceRequests() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
            SELECT t.ticket_id, t.client_id, t.title, t.description, t.status, ts.solution_description 
            FROM tickets t
            LEFT JOIN ticket_solutions ts ON t.ticket_id = ts.ticket_id
            WHERE t.status != 'Resolved'
        """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== Service Requests =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Ticket ID: " + rs.getInt("ticket_id"));
                System.out.println("Client ID: " + rs.getInt("client_id"));
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Description: " + rs.getString("description"));
                String solution = rs.getString("solution_description");
                if (solution != null) {
                    System.out.println("Last Solution: " + solution);
                } else {
                    System.out.println("No solution provided yet.");
                }
                System.out.println("Status: " + rs.getString("status"));
            }
            if (!found) System.out.println("No active service requests.");

        } catch (SQLException e) {
            System.out.println("viewServiceRequests not working");
        }
    }

    private void askForClarification() {
        System.out.print("Enter Ticket ID to request clarification: ");
        int ticketId = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter your clarification question: ");
        String question = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO clarifications (ticket_id, question) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, ticketId);
            stmt.setString(2, question);
            stmt.executeUpdate();

            System.out.println("Clarification request sent to the client.");

        } catch (SQLException e) {
            System.out.println("askForClarification not working");
        }
    }

    private void viewClientResponses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT c.clarification_id, c.ticket_id, c.question, c.answer 
                FROM clarifications c
                JOIN tickets t ON c.ticket_id = t.ticket_id
                WHERE c.answer IS NOT NULL
            """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== Client Responses =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Clarification ID: " + rs.getInt("clarification_id"));
                System.out.println("Ticket ID: " + rs.getInt("ticket_id"));
                System.out.println("Question: " + rs.getString("question"));
                System.out.println("Client Response: " + rs.getString("answer"));
                System.out.println("----------------------");
            }

            if (!found) System.out.println("No responses from clients yet.");

        } catch (SQLException e) {
            System.out.println("viewClientResponses not working");
        }
    }

    private void resolveRequest() {
        System.out.print("Enter Ticket ID to resolve: ");
        int ticketId = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter your solution description: ");
        String solutionDescription = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE tickets SET status = 'Resolved', solution_description = ?, resolved_date = NOW() WHERE ticket_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, solutionDescription);
            stmt.setInt(2, ticketId);
            stmt.executeUpdate();

            System.out.println("Service request resolved successfully!");

        } catch (SQLException e) {
            System.out.println("resolveRequest not working");
        }
    }

    private void viewMonthlyResolvedRequests() {
        System.out.print("Enter year (YYYY): ");
        int year = scanner.nextInt();
        System.out.print("Enter month (MM): ");
        int month = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update the query to join clarifications and get the latest response from vendor
            String query = """
            SELECT ticket_id, title, solution_description, resolved_date, status 
            FROM tickets 
            WHERE status = 'Resolved' 
            AND YEAR(resolved_date) = ? 
            AND MONTH(resolved_date) = ?;
        """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== Resolved Tickets for " + year + "-" + month + " =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Ticket ID: " + rs.getInt("ticket_id"));
                System.out.println("Title: " + rs.getString("title"));
//                System.out.println("Description: " + rs.getString("description"));
                System.out.println("Resolved Date: " + rs.getDate("resolved_date"));
                String vendorResponse = rs.getString("vendor_response");
                if (vendorResponse != null) {
                    System.out.println("Vendor Response: " + vendorResponse);
                } else {
                    System.out.println("Vendor Response: No response provided.");
                }
                System.out.println("----------------------");
            }

            if (!found) System.out.println("No resolved tickets found for this month.");

        } catch (SQLException e) {
            System.out.println("viewMonthlyResolvedRequests not working");
        }
    }


//    private void viewWeeklyResolvedRequests() {
//        System.out.print("Enter year (YYYY): ");
//        int year = scanner.nextInt();
//        System.out.print("Enter week number (1-52): ");
//        int weekNumber = scanner.nextInt();
//        scanner.nextLine(); // Consume newline
//
//        try (Connection conn = DatabaseConnection.getConnection()) {
//            String query = """
//                SELECT ticket_id, description, resolved_date
//                FROM tickets
//                WHERE status = 'Resolved'
//                AND YEAR(resolved_date) = ?
//                AND WEEK(resolved_date, 1) = ?;
//            """;
//            PreparedStatement stmt = conn.prepareStatement(query);
//            stmt.setInt(1, year);
//            stmt.setInt(2, weekNumber);
//            ResultSet rs = stmt.executeQuery();
//
//            System.out.println("\n===== Resolved Tickets for Week " + weekNumber + " of " + year + " =====");
//            boolean found = false;
//            while (rs.next()) {
//                found = true;
//                System.out.println("Ticket ID: " + rs.getInt("ticket_id"));
//                System.out.println("Description: " + rs.getString("description"));
//                System.out.println("Resolved Date: " + rs.getString("resolved_date"));
//                System.out.println("----------------------");
//            }
//
//            if (!found) System.out.println("No tickets resolved in this week.");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    private void viewPendingRequestsCount() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM tickets WHERE status = 'Pending'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Number of Pending Tickets: " + count);
            }

        } catch (SQLException e) {
            System.out.println("viewPendingRequestsCount not working");
        }
    }
}
