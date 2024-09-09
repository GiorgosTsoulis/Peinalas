import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderManager extends JFrame {

    private int storeId;
    private JPanel ordersPanel;
    private JScrollPane scrollPanel;

    public OrderManager(int storeId) {
        this.setTitle("Order Manager");
        this.storeId = storeId;

        // Initialize the frame layout
        initializeLayout();

        // Load and display active orders
        loadOrders();

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeLayout() {
        setLayout(new BorderLayout());

        ordersPanel = new JPanel();
        ordersPanel.setLayout(new GridBagLayout());
        scrollPanel = new JScrollPane(ordersPanel);

        // Back button
        JButton backBtn = new JButton("Back");
        JPanel backBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backBtnPanel.add(backBtn);

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OrderManager.this.dispose();
            }
        });

        add(scrollPanel, BorderLayout.CENTER);
        add(backBtnPanel, BorderLayout.SOUTH);
    }

    private void loadOrders() {
        // Clear the panel before reloading
        ordersPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            String query = "SELECT o.order_id, o.total_amount, o.status, o.service_type, o.order_date "
                    + "FROM Orders o "
                    + "JOIN Stores st ON o.store_id = st.store_id "
                    + "WHERE o.store_id = ? AND o.status = 'In Progress'";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, storeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                double totalAmount = rs.getDouble("total_amount");
                String status = rs.getString("status");
                String serviceType = rs.getString("service_type");
                String orderDate = rs.getString("order_date");

                // Create a panel for each order
                JPanel orderPanel = new JPanel();
                orderPanel.setLayout(new GridBagLayout());
                orderPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Optional: border for separation

                GridBagConstraints orderGbc = new GridBagConstraints();
                orderGbc.insets = new Insets(5, 5, 5, 5);
                orderGbc.anchor = GridBagConstraints.WEST;
                orderGbc.fill = GridBagConstraints.HORIZONTAL;

                // Add order details
                JLabel orderLabel = new JLabel("<html>Order ID: " + orderId + "<br>Amount: $" + totalAmount
                        + "<br>Service: " + serviceType + "<br>Status: " + status + "<br>Date: " + orderDate
                        + "</html>");
                orderGbc.gridx = 0;
                orderGbc.gridy = 0;
                orderGbc.gridwidth = 3;
                orderPanel.add(orderLabel, orderGbc);

                // Add buttons in the same row
                JButton confirmButton = new JButton("Confirm");
                JButton cancelButton = new JButton("Cancel");
                JButton completeButton = new JButton("Complete");

                orderGbc.gridy = 1;
                orderGbc.gridwidth = 1;
                orderGbc.gridx = 0;
                orderPanel.add(confirmButton, orderGbc);

                orderGbc.gridx = 1;
                orderPanel.add(cancelButton, orderGbc);

                orderGbc.gridx = 2;
                orderPanel.add(completeButton, orderGbc);

                gbc.gridy++;
                gbc.gridx = 0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                ordersPanel.add(orderPanel, gbc);

                // Button action listeners to update order status
                confirmButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateOrderStatus(orderId, "Confirmed");
                    }
                });

                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateOrderStatus(orderId, "Cancelled");

                    }
                });

                completeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateOrderStatus(orderId, "Completed");
                    }
                });

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Refresh panel after loading
        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    private void updateOrderStatus(int orderId, String status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            // Check if the order exists
            String checkQuery = "SELECT order_id FROM Orders WHERE order_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, orderId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Update the order status
                String updateQuery = "UPDATE Orders SET status = ? WHERE order_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, status);
                updateStmt.setInt(2, orderId);
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Order status updated. Triggering email notification.");
                    OrderNotificationScheduler.checkOrdersAndSendNotifications(orderId);
                    JOptionPane.showMessageDialog(this, "Status changed successfully and email sent to client.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to change the status. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Order ID not found.");
            }

            loadOrders(); // Reload the orders after updating status

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}