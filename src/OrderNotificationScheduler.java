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
        String emailHeader = "Dear Valued Customer,\n\n";
        String emailFooter = "\n\nThank you for choosing Peinalas. If you have any questions, feel free to contact our support team.\n\nBest regards,\nPeinalas Team";

        switch (status) {
            case "In Progress":
                return emailHeader +
                        "Great news! Your order #" + orderId
                        + "has been confirmed and is currently being prepared. We’ll notify you once it's ready." +
                        emailFooter;
            case "Completed":
                return emailHeader +
                        "Your order #" + orderId
                        + " is now complete and ready for pickup or delivery. We hope you enjoy your meal!" +
                        emailFooter;
            case "Cancelled":
                return emailHeader +
                        "We regret to inform you that your order #" + orderId + " has been cancelled.\n" +
                        "If you have any concerns or need assistance, please reach out to us." +
                        emailFooter;
            case "Pending":
                return emailHeader +
                        "Your order #" + orderId
                        + " is awaiting confirmation. You’ll receive a notification once it has been confirmed." +
                        emailFooter;
            default:
                return emailHeader +
                        "We have an important update regarding your order #" + orderId + ".\n" +
                        "Please check the status in our app for more details." +
                        emailFooter;
        }
    }

}
