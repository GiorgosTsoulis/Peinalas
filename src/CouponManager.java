import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CouponManager extends JFrame {

    private int storeId;
    private JPanel couponGeneratorPanel;
    private JScrollPane couponGeneratorScrollPane;
    private JTable couponTable;
    private JTextField discountField, expirationField, codeField, minOrderField;
    private JLabel discountLabel, expirationLabel, codeLabel, minOrderLabel;
    private JButton generateButton;

    public CouponManager(int storeId) {
        this.storeId = storeId;

        initializeLayout();
    }

    private void initializeLayout() {
        this.setTitle("Coupon Manager");
        this.setLayout(new BorderLayout());

        // Coupon Generator Panel
        couponGeneratorPanel = new JPanel();
        couponGeneratorPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Coupon Generator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        couponGeneratorPanel.add(titleLabel, gbc);

        discountLabel = new JLabel("Discount %:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        couponGeneratorPanel.add(discountLabel, gbc);

        discountField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 1;
        couponGeneratorPanel.add(discountField, gbc);

        expirationLabel = new JLabel("Expiration Date:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        couponGeneratorPanel.add(expirationLabel, gbc);

        expirationField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 2;
        couponGeneratorPanel.add(expirationField, gbc);

        minOrderLabel = new JLabel("Min Order Value:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        couponGeneratorPanel.add(minOrderLabel, gbc);

        minOrderField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 3;
        couponGeneratorPanel.add(minOrderField, gbc);

        codeLabel = new JLabel("Code:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        couponGeneratorPanel.add(codeLabel, gbc);

        codeField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 4;
        couponGeneratorPanel.add(codeField, gbc);

        generateButton = new JButton("Generate");
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        couponGeneratorPanel.add(generateButton, gbc);

        // Action for generate button
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateCoupon();
            }
        });

        couponGeneratorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Coupon Table Panel
        couponTable = new JTable();
        couponGeneratorScrollPane = new JScrollPane(couponTable);

        populateCouponTable();

        this.add(couponGeneratorPanel, BorderLayout.NORTH);
        this.add(couponGeneratorScrollPane, BorderLayout.CENTER);

        this.setPreferredSize(new Dimension(600, 500));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void generateCoupon() {
        String discountText = discountField.getText();
        String expirationDate = expirationField.getText();

        if (discountText.isEmpty() || expirationDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.");
            return;
        }

        String couponCode = generateRandomCouponCode();

        codeField.setText(couponCode);

        try {
            double discount = Double.parseDouble(discountText);
            if (discount <= 0) {
                JOptionPane.showMessageDialog(this, "Discount must be greater than 0.");
                return;
            }

            String query = "INSERT INTO Coupons (coupon_code, discount_amount, expiry_date, store_id, usage_limit) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Database connection error.");
                    return;
                }

                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, couponCode);
                pstmt.setDouble(2, discount);
                pstmt.setString(3, expirationDate);
                pstmt.setInt(4, storeId);
                pstmt.setInt(5, 1);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Coupon generated successfully!");
                    populateCouponTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to generate coupon.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid discount amount.");
        }
    }

    private String generateRandomCouponCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder couponCode = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            couponCode.append(chars.charAt(randomIndex));
        }
        return couponCode.toString();
    }

    private void populateCouponTable() {
        DefaultTableModel tableModel = new DefaultTableModel();

        tableModel.setColumnIdentifiers(new Object[] { "Coupon ID", "Coupon Code", "Discount", "Expiry Date",
                "Min Order Value", "Usage Limit" });
        couponTable.setModel(tableModel);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            String query = "SELECT coupon_id, coupon_code, discount_amount, expiry_date, min_order_value, usage_limit FROM Coupons WHERE store_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, storeId);
            ResultSet rs = pstmt.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                int couponId = rs.getInt("coupon_id");
                String couponCode = rs.getString("coupon_code");
                double discountAmount = rs.getDouble("discount_amount");
                String expiryDate = rs.getString("expiry_date");
                double minOrderValue = rs.getDouble("min_order_value");
                int usageLimit = rs.getInt("usage_limit");

                tableModel.addRow(new Object[] { couponId, couponCode, discountAmount, expiryDate,
                        minOrderValue, usageLimit });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
