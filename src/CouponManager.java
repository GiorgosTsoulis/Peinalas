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
    private JLabel discountLabel, expirationLabel, codeLabel, minOrderLabel, discountTypeLabel;
    private JComboBox<String> discountTypeComboBox;
    private JButton generateButton;

    public CouponManager(int storeId) {
        this.storeId = storeId;
        System.out.println("Store ID: " + storeId);

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

        discountLabel = new JLabel("Discount:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        couponGeneratorPanel.add(discountLabel, gbc);

        discountField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 1;
        couponGeneratorPanel.add(discountField, gbc);

        discountTypeLabel = new JLabel("Discount Type:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        couponGeneratorPanel.add(discountTypeLabel, gbc);

        String discountTypes[] = { "Percentage", "Flat" };
        discountTypeComboBox = new JComboBox<>(discountTypes);
        gbc.gridx = 1;
        gbc.gridy = 2;
        couponGeneratorPanel.add(discountTypeComboBox, gbc);

        expirationLabel = new JLabel("Expiration Date:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        couponGeneratorPanel.add(expirationLabel, gbc);

        expirationField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 3;
        couponGeneratorPanel.add(expirationField, gbc);

        minOrderLabel = new JLabel("Min Order Value:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        couponGeneratorPanel.add(minOrderLabel, gbc);

        minOrderField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 4;
        couponGeneratorPanel.add(minOrderField, gbc);

        codeLabel = new JLabel("Code:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        couponGeneratorPanel.add(codeLabel, gbc);

        codeField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 5;
        couponGeneratorPanel.add(codeField, gbc);

        generateButton = new JButton("Generate");
        gbc.gridx = 1;
        gbc.gridy = 6;
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

        // Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel backBtnPanel = new JPanel();
        backBtnPanel.setLayout(new GridBagLayout());
        GridBagConstraints backgbc = new GridBagConstraints();
        backgbc.insets = new Insets(5, 5, 5, 5);
        backgbc.fill = GridBagConstraints.HORIZONTAL;

        backgbc.gridx = 0;
        backgbc.gridy = 0;
        backBtnPanel.add(backButton, backgbc);

        this.add(couponGeneratorPanel, BorderLayout.NORTH);
        this.add(couponGeneratorScrollPane, BorderLayout.CENTER);
        this.add(backBtnPanel, BorderLayout.SOUTH);

        this.setPreferredSize(new Dimension(600, 500));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void generateCoupon() {
        String discountText = discountField.getText();
        String discountType = discountTypeComboBox.getSelectedItem().toString();
        String expirationDate = expirationField.getText();
        String minOrderValueText = minOrderField.getText();

        // Validate the expiration date format
        if (expirationDate != null && !expirationDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid expiration date in the format YYYY-MM-DD.");
            return;
        }

        // Ensure all required fields are filled
        if (discountText.isEmpty() || expirationDate.isEmpty() || minOrderValueText.isEmpty()
                || discountType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.");
            return;
        }

        // Generate the coupon code
        String couponCode = generateRandomCouponCode();
        codeField.setText(couponCode);

        try {
            // Parse and validate discount amount
            double discount = Double.parseDouble(discountText);
            if (discount <= 0) {
                JOptionPane.showMessageDialog(this, "Discount must be greater than 0.");
                return;
            }

            // Parse and validate minimum order value
            double minOrderValue = Double.parseDouble(minOrderValueText);
            if (minOrderValue <= 0) {
                JOptionPane.showMessageDialog(this, "Minimum order value must be greater than 0.");
                return;
            }

            String query = "INSERT INTO Coupons (coupon_code, discount_amount, discount_type, expiry_date, min_order_value, store_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Database connection error.");
                    return;
                }

                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, couponCode);
                pstmt.setDouble(2, discount);
                pstmt.setString(3, discountType);
                pstmt.setString(4, expirationDate);
                pstmt.setDouble(5, minOrderValue);
                pstmt.setInt(6, storeId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Coupon generated successfully!");
                    populateCouponTable();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to generate coupon.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for discount and minimum order value.");
        }
    }

    private void clearFields() {
        discountField.setText("");
        expirationField.setText("");
        minOrderField.setText("");
        codeField.setText("");
        discountTypeComboBox.setSelectedIndex(0); // Reset to first item in the combo box
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

        tableModel.setColumnIdentifiers(new Object[] { "Coupon ID", "Discount", "Discount Type", "Expiry Date",
                "Min Order Value", "Coupon Code" });
        couponTable.setModel(tableModel);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            String query = "SELECT coupon_id, discount_amount, discount_type, expiry_date, min_order_value, coupon_code FROM Coupons WHERE store_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, storeId);
            ResultSet rs = pstmt.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                int couponId = rs.getInt("coupon_id");
                double discountAmount = rs.getDouble("discount_amount");
                String discountType = rs.getString("discount_type");
                String expiryDate = rs.getString("expiry_date");
                double minOrderValue = rs.getDouble("min_order_value");
                String couponCode = rs.getString("coupon_code");

                tableModel.addRow(new Object[] { couponId, discountAmount, discountType, expiryDate,
                        minOrderValue, couponCode });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
