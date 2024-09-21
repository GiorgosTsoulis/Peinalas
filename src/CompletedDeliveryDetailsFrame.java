import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.*;
import javax.swing.*;

public class CompletedDeliveryDetailsFrame extends JFrame {

    private JPanel orderDetailsPanel, customerDetailsPanel;
    @SuppressWarnings("unused")
    private int orderId;
    @SuppressWarnings("unused")
    private DeliveryDashboardFrame deliveryDashboardFrame;

    public CompletedDeliveryDetailsFrame(int orderId, DeliveryDashboardFrame deliveryDashboardFrame) {
        this.orderId = orderId;
        this.deliveryDashboardFrame = deliveryDashboardFrame;
        this.setTitle("Pending Delivery Details - Order ID: " + orderId);
        this.setLayout(new BorderLayout());

        // Use a panel with BoxLayout to stack labels vertically
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(1, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel for order details
        orderDetailsPanel = new JPanel();
        orderDetailsPanel.setLayout(new BoxLayout(orderDetailsPanel, BoxLayout.Y_AXIS));
        orderDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        detailsPanel.add(orderDetailsPanel);

        // Panel for customer details
        customerDetailsPanel = new JPanel();
        customerDetailsPanel.setLayout(new BoxLayout(customerDetailsPanel, BoxLayout.Y_AXIS));
        customerDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        detailsPanel.add(customerDetailsPanel);

        // Load order and customer details
        loadOrderDetails(orderId);
        loadCustomerDetails(orderId);

        // Items ordered panel
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items Ordered"));

        JTable itemsTable = new JTable(new DefaultTableModel(
                new String[] { "Item Name", "Quantity", "Price" }, 0));
        loadOrderItems(orderId, (DefaultTableModel) itemsTable.getModel());
        itemsPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Back Button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> this.dispose());
        backButton.setPreferredSize(new Dimension(100, 25));
        buttonsPanel.add(backButton);

        // Add panels to frame
        this.add(detailsPanel, BorderLayout.NORTH);
        this.add(itemsPanel, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.SOUTH);

        // Frame settings
        this.setSize(600, 400);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
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

    private void loadCustomerDetails(int orderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.user_id, u.firstname, u.lastname, CONCAT(u.address, ' ', u.address_number) AS address, "
                    +
                    "u.post_code, u.number " +
                    "FROM Orders o " +
                    "JOIN Users u ON o.user_id = u.user_id " +
                    "WHERE o.order_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, orderId);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                JLabel customerIdLabel = new JLabel("Customer ID: " + resultSet.getInt("user_id"));
                JLabel customerNameLabel = new JLabel(
                        "Customer: " + resultSet.getString("firstname") + " " + resultSet.getString("lastname"));
                JLabel addressLabel = new JLabel(
                        "Address: " + resultSet.getString("address") + ", " + resultSet.getString("post_code"));
                JLabel phoneLabel = new JLabel("Phone: " + resultSet.getString("number"));

                customerDetailsPanel.add(customerIdLabel);
                customerDetailsPanel.add(customerNameLabel);
                customerDetailsPanel.add(addressLabel);
                customerDetailsPanel.add(phoneLabel);
            } else {
                JOptionPane.showMessageDialog(this, "Customer details not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadOrderDetails(int orderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT o.order_id, o.store_id, s.name AS store_name, o.status, o.total_amount " +
                    "FROM Orders o " +
                    "JOIN Stores s ON o.store_id = s.store_id " +
                    "WHERE o.order_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, orderId);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                JLabel orderIdLabel = new JLabel("Order ID: " + resultSet.getInt("order_id"));
                JLabel storeIdLabel = new JLabel("Store ID: " + resultSet.getInt("store_id"));
                JLabel storeLabel = new JLabel("Store: " + resultSet.getString("store_name"));
                JLabel statusLabel = new JLabel("Status: " + resultSet.getString("status"));
                JLabel totalAmountLabel = new JLabel("Total Amount: $" + resultSet.getDouble("total_amount"));

                orderDetailsPanel.add(orderIdLabel);
                orderDetailsPanel.add(storeIdLabel);
                orderDetailsPanel.add(storeLabel);
                orderDetailsPanel.add(totalAmountLabel);
                orderDetailsPanel.add(statusLabel);
            } else {
                JOptionPane.showMessageDialog(this, "Order details not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
