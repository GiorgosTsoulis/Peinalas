import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class applyPromoFrame extends JFrame {

    private int orderId;

    applyPromoFrame(int orderId) {
        this.orderId = orderId;

        // Create a new frame
        this.setTitle("Apply Promo Code");

        // Create a panel with GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        this.add(panel);

        // Create GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create components
        JLabel promoLabel = new JLabel("Enter Promo Code:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(promoLabel, gbc);

        JTextField promoField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(promoField, gbc);

        JButton applyButton = new JButton("Apply");
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(applyButton, gbc);

        // Action for apply button
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String promoCode = promoField.getText();
                applyPromo(promoCode);
                dispose();
            }
        });

        this.setPreferredSize(new Dimension(500, 300));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void applyPromo(String promoCode) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            if (!isValidPromo(promoCode, conn)) {
                JOptionPane.showMessageDialog(this, "Invalid or expired promo code.");
                return;
            }

            // Fetch the discount amount and type from the Coupons table
            String query = "SELECT discount_amount, discount_type FROM Coupons WHERE coupon_code = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, promoCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double discount = rs.getDouble("discount_amount");
                String discountType = rs.getString("discount_type");

                // Apply discount based on the type (percentage or fixed)
                if (discountType.equals("Percentage")) {
                    // Update the total amount by applying percentage discount
                    String updateOrderQuery = "UPDATE Orders SET total_amount = total_amount * (1 - ? / 100) WHERE order_id = ?";
                    PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderQuery);
                    updateOrderStmt.setDouble(1, discount);
                    updateOrderStmt.setInt(2, orderId);
                    updateOrderStmt.executeUpdate();
                } else if (discountType.equals("Flat")) {
                    // Update the total amount by subtracting the fixed discount
                    String updateOrderQuery = "UPDATE Orders SET total_amount = total_amount - ? WHERE order_id = ?";
                    PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderQuery);
                    updateOrderStmt.setDouble(1, discount);
                    updateOrderStmt.setInt(2, orderId);
                    updateOrderStmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Promo code applied successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Promo code not found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while applying the promo code.");
        }
    }

    public boolean isValidPromo(String promoCode, Connection conn) {
        try {
            String query = "SELECT * FROM Coupons WHERE coupon_code = ? AND expiry_date > NOW() AND usage_limit = 1";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, promoCode);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
