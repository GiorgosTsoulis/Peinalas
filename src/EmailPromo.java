import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class EmailPromo {

    public static void sendPromoEmails(Connection conn, int storeId, int userId) throws SQLException {
        String query = "SELECT u.email, c.coupon_code, c.discount_amount, c.expiry_date " +
                "FROM Users u " +
                "JOIN Orders o ON u.user_id = o.user_id " +
                "JOIN Coupons c ON o.store_id = c.store_id " +
                "WHERE o.store_id = ? AND o.user_id = ? AND c.expiry_date >= CURDATE()";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, storeId); // Set the specific store ID
            stmt.setInt(2, userId); // Set the specific user ID

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { // Fetch only the first result
                    String email = rs.getString("email");
                    String couponCode = rs.getString("coupon_code");
                    double discountAmount = rs.getDouble("discount_amount");
                    String expiryDate = rs.getString("expiry_date");

                    String subject = "Exclusive Promo Just for You!";
                    String body = createPromoEmailBody(couponCode, discountAmount, expiryDate);

                    EmailSender.sendEmail(email, subject, body);
                }
            }
        }
    }

    public static void checkAndSendPromoEmail(int lastOrderId, int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            // Retrieve the storeId from the database
            String storeIdQuery = "SELECT store_id FROM Orders WHERE order_id = ?";
            PreparedStatement storeIdStmt = conn.prepareStatement(storeIdQuery);
            storeIdStmt.setInt(1, lastOrderId);
            ResultSet storeIdRs = storeIdStmt.executeQuery();

            int storeId = -1;
            if (storeIdRs.next()) {
                storeId = storeIdRs.getInt("store_id");
            }

            // Check the order count
            String orderCountQuery = "SELECT order_count FROM PromoEmailTracker WHERE user_id = ? AND store_id = ?";
            PreparedStatement orderCountStmt = conn.prepareStatement(orderCountQuery);
            orderCountStmt.setInt(1, userId);
            orderCountStmt.setInt(2, storeId);
            ResultSet orderCountRs = orderCountStmt.executeQuery();

            boolean shouldSendEmail = false;
            int orderCount = 0;
            if (orderCountRs.next()) {
                orderCount = orderCountRs.getInt("order_count");
                if (orderCount >= 2) { // Check if the count is 2 because it will be incremented later
                    shouldSendEmail = true;
                }
            } else {
                // If no record exists, insert a new record with order_count = 1
                String insertPromoEmailTrackerQuery = "INSERT INTO PromoEmailTracker (user_id, store_id, order_count) VALUES (?, ?, 1)";
                PreparedStatement insertPromoEmailTrackerStmt = conn.prepareStatement(insertPromoEmailTrackerQuery);
                insertPromoEmailTrackerStmt.setInt(1, userId);
                insertPromoEmailTrackerStmt.setInt(2, storeId);
                insertPromoEmailTrackerStmt.executeUpdate();
                return; // Exit the method as the count is just initialized
            }

            if (shouldSendEmail) {
                sendPromoEmails(conn, storeId, userId); // Pass connection to sendPromoEmails

                // Reset the order count to 0 after sending the email
                String resetOrderCountQuery = "UPDATE PromoEmailTracker SET order_count = 0 WHERE user_id = ? AND store_id = ?";
                PreparedStatement resetOrderCountStmt = conn.prepareStatement(resetOrderCountQuery);
                resetOrderCountStmt.setInt(1, userId);
                resetOrderCountStmt.setInt(2, storeId);
                resetOrderCountStmt.executeUpdate();
            } else {
                // Increment the order count
                String incrementOrderCountQuery = "UPDATE PromoEmailTracker SET order_count = order_count + 1 WHERE user_id = ? AND store_id = ?";
                PreparedStatement incrementOrderCountStmt = conn.prepareStatement(incrementOrderCountQuery);
                incrementOrderCountStmt.setInt(1, userId);
                incrementOrderCountStmt.setInt(2, storeId);
                incrementOrderCountStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while checking and sending promo emails.");
        }
    }

    private static String createPromoEmailBody(String couponCode, double discountAmount, String expiryDate) {
        return "Dear Customer,\n\n" +
                "We are excited to offer you an exclusive promo code for your next order!\n" +
                "Use the coupon code: " + couponCode + "\n" +
                "Discount: $" + discountAmount + "\n" +
                "Expires on: " + expiryDate + "\n\n" +
                "Don't miss out on this limited-time offer!\n" +
                "Best regards,\n" +
                "Peinalas Team";
    }
}
