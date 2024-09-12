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
    private Runnable onPromoApplied;

    applyPromoFrame(int orderId, Runnable onPromoApplied) {
        this.orderId = orderId;
        this.onPromoApplied = onPromoApplied;
        System.out.println("orderId: " + orderId);

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

        JButton skipButton = new JButton("Skip");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(skipButton, gbc);

        // Action for apply button
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String promoCode = promoField.getText();
                applyPromo(promoCode);
                dispose();
            }
        });

        // Action for skip button
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPromoApplied.run();
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

            // Fetch the total amount from the OrderItems table
            double totalAmount = fetchTotalAmount(conn);
            if (totalAmount == -1) {
                JOptionPane.showMessageDialog(this, "Failed to fetch total amount.");
                return;
            }

            // Fetch the discount amount and type from the Coupons table
            String query = "SELECT discount_amount, discount_type FROM Coupons WHERE coupon_code = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, promoCode);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double discount = rs.getDouble("discount_amount");
                        String discountType = rs.getString("discount_type");

                        // Apply discount based on the type (percentage or fixed)
                        if (discountType.equals("Percentage")) {
                            totalAmount = totalAmount * (1 - discount / 100);
                        } else if (discountType.equals("Flat")) {
                            totalAmount = totalAmount - discount;
                        } else {
                            JOptionPane.showMessageDialog(this, "Unknown discount type.");
                            return;
                        }

                        // Update the Orders table with the new total amount
                        String updateOrderQuery = "UPDATE Orders SET total_amount = ? WHERE order_id = ?";
                        try (PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderQuery)) {
                            updateOrderStmt.setDouble(1, totalAmount);
                            updateOrderStmt.setInt(2, orderId);
                            updateOrderStmt.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(this, "Promo code applied successfully!");
                        if (onPromoApplied != null) {
                            onPromoApplied.run();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Promo code not found.");
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while applying the promo code.");
        }
    }

    private double fetchTotalAmount(Connection conn) {
        String query = "SELECT SUM(price) AS total_amount FROM OrderItems WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_amount");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public boolean isValidPromo(String promoCode, Connection conn) {
        String query = "SELECT * FROM Coupons WHERE coupon_code = ? AND expiry_date > NOW() AND usage_limit = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, promoCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}