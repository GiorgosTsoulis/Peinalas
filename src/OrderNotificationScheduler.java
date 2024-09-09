import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderNotificationScheduler {

    public static void checkOrdersAndSendNotifications(int orderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query for a specific order that needs notification and hasn't been notified
            // yet
            String query = "SELECT o.order_id, u.email, o.status, o.timer_end " +
                    "FROM Orders o " +
                    "JOIN Users u ON o.user_id = u.user_id " +
                    "WHERE o.order_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, orderId); // Set the specific order ID
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String email = rs.getString("email");
                        String status = rs.getString("status");

                        String subject = "Update on Your Order Status";
                        String body = createNotificationBody(orderId, status);

                        EmailSender.sendEmail(email, subject, body);

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String createNotificationBody(int orderId, String status) {
        switch (status) {
            case "Confirmed":
                return "Dear Customer,\n\nYour order with ID " + orderId + " has been confirmed.\n\n" +
                        "Thank you for choosing our service!\n\n" +
                        "Best regards,\n" +
                        "Peinalas Team";
            case "In Progress":
                return "Dear Customer,\n\nYour order with ID " + orderId + " is currently in progress.\n\n" +
                        "We will update you once it's completed.\n\n" +
                        "Best regards,\n" +
                        "Peinalas Team";
            case "Completed":
                return "Dear Customer,\n\nYour order with ID " + orderId
                        + " has been completed and is ready for pickup.\n\n" +
                        "Thank you for using our service!\n\n" +
                        "Best regards,\n" +
                        "Peinalas Team";
            case "Cancelled":
                return "Dear Customer,\n\nYour order with ID " + orderId + " has been cancelled.\n\n" +
                        "If you have any questions, please contact us.\n\n" +
                        "Best regards,\n" +
                        "Peinalas Team";
            default:
                return "Dear Customer,\n\nWe have an update on your order with ID " + orderId + ".\n\n" +
                        "Please check your order status in our app for more details.\n\n" +
                        "Best regards,\n" +
                        "Peinalas Team";
        }
    }
}
