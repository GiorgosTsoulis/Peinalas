import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class OrderDetails extends JFrame {

    private String orderStatus;
    private OrderManager orderManager;

    public OrderDetails(int orderId, OrderManager orderManager) {
        this.orderManager = orderManager;
        this.setTitle("Order Details - Order ID: " + orderId);
        this.setLayout(new BorderLayout());

        // Order details panel
        JPanel detailsPanel = new JPanel(new GridLayout(5, 1));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Load order details
        orderStatus = loadOrderDetails(orderId, detailsPanel);

        // Items panel (List of items)
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items Ordered"));

        JTable itemsTable = new JTable(new DefaultTableModel(
                new String[] { "Item Name", "Quantity", "Price" }, 0));
        loadOrderItems(orderId, (DefaultTableModel) itemsTable.getModel());
        itemsPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        // Complete Button
        if ("In Progress".equals(orderStatus)) {
            JButton completeButton = new JButton("Complete Order");
            completeButton.addActionListener(e -> completeOrder(orderId));
            this.add(completeButton, BorderLayout.SOUTH);
        }

        // Add panels to the frame
        this.add(detailsPanel, BorderLayout.NORTH);
        this.add(itemsPanel, BorderLayout.CENTER);

        this.setSize(400, 300);
        this.setLocationRelativeTo(null); // Center the window
        this.setVisible(true);
    }

    private String loadOrderDetails(int orderId, JPanel panel) {
        String status = "";
        String query = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    panel.add(new JLabel("Order ID: " + rs.getInt("order_id")));
                    panel.add(new JLabel("User ID: " + rs.getInt("user_id")));
                    panel.add(new JLabel("Total Amount: $" + rs.getDouble("total_amount")));
                    panel.add(new JLabel("Service Type: " + rs.getString("service_type")));
                    status = rs.getString("status");
                    panel.add(new JLabel("Status: " + status));
                } else {
                    JOptionPane.showMessageDialog(this, "Order not found.");
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading order details.");
            e.printStackTrace();
        }
        return status;
    }

    private void loadOrderItems(int orderId, DefaultTableModel tableModel) {
        String query = "SELECT m.item_name, oi.quantity, oi.price FROM OrderItems oi " +
                "JOIN MenuItems m ON oi.item_id = m.item_id " +
                "WHERE oi.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String itemName = rs.getString("item_name");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");
                    tableModel.addRow(new Object[] { itemName, quantity, price });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading order items.");
            e.printStackTrace();
        }
    }

    private void completeOrder(int orderId) {
        String query = "SELECT service_type FROM Orders WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String serviceType = rs.getString("service_type");

                    String updateQuery;
                    if ("Delivery".equals(serviceType)) {
                        updateQuery = "UPDATE Orders SET status = 'Ready for delivery' WHERE order_id = ?";
                    } else if ("Takeaway".equals(serviceType) || "Dine-in".equals(serviceType)) {
                        updateQuery = "UPDATE Orders SET status = 'Completed' WHERE order_id = ?";
                    } else {
                        JOptionPane.showMessageDialog(this, "Unknown service type: " + serviceType);
                        return;
                    }

                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                        updatePstmt.setInt(1, orderId);
                        int rowsAffected = updatePstmt.executeUpdate();

                        if (rowsAffected > 0) {
                            OrderNotificationScheduler.checkOrdersAndSendNotifications(orderId);
                            JOptionPane.showMessageDialog(this, "Order updated successfully.");
                            this.dispose(); // Close the window

                            if (orderManager != null) {
                                orderManager.refreshTables(); // Refresh the tables in OrderManager
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Order not found or cannot be updated.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Order not found.");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating the order.");
            ex.printStackTrace();
        }
    }

}
