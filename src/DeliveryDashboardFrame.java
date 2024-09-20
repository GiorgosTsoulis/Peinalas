import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DeliveryDashboardFrame extends DashboardFrame {

    private JPanel deliveryDetailsPanel, buttonPanel, pendingDeliveriesPanel, activeDeliveriesPanel;
    private JTabbedPane deliveriesTabbedPane;
    private JToggleButton deliveryStatusButton;
    private JButton logoutButton;
    private JLabel deliveryStatusLabel;

    private int userId;

    public DeliveryDashboardFrame(int userId) {
        super("Delivery");
        this.userId = userId;

        initializeLayout();

        this.setPreferredSize(new Dimension(800, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeLayout() {
        setLayout(new BorderLayout());

        deliveryDetailsPanel = new JPanel();
        deliveryDetailsPanel.setLayout(new BoxLayout(deliveryDetailsPanel, BoxLayout.Y_AXIS));
        deliveryDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        deliveriesTabbedPane = new JTabbedPane();
        pendingDeliveriesPanel = new JPanel(new BorderLayout());
        activeDeliveriesPanel = new JPanel(new BorderLayout());
        deliveriesTabbedPane.addTab("Pending Deliveries", pendingDeliveriesPanel);
        deliveriesTabbedPane.addTab("Active Deliveries", activeDeliveriesPanel);
        deliveriesTabbedPane.setVisible(false);
        add(deliveriesTabbedPane, BorderLayout.EAST);

        initializeStatus("Not Available");

        displayDeliveryDetails();

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        deliveryStatusButton = new JToggleButton("Available/Not Available");
        logoutButton = new JButton("Logout");

        deliveryStatusButton.addActionListener(e -> toggleDeliveryStatus());
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginFrame();
            }
        });

        buttonPanel.add(deliveryStatusButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(logoutButton);

        deliveryDetailsPanel.add(buttonPanel);

        add(deliveryDetailsPanel, BorderLayout.CENTER);
    }

    private void initializeStatus(String status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE Delivery SET delivery_status = ? WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();

            refreshDeliveryLabelStatus(status);
            refreshDeliveryPanelVisibility(status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayDeliveryDetails() {
        deliveryDetailsPanel.removeAll();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.firstname, u.lastname, u.email, u.number, d.delivery_id, d.vehicle, d.license_plate, d.delivery_status FROM Users u, Delivery d WHERE u.user_id = d.user_id AND d.user_id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                JLabel deliveryIdLabel = new JLabel("Delivery ID: " + result.getInt("delivery_id"));
                JLabel firstnameLabel = new JLabel("Firstname: " + result.getString("firstname"));
                JLabel lastnameLabel = new JLabel("Lastname: " + result.getString("lastname"));
                JLabel emailLabel = new JLabel("Email: " + result.getString("email"));
                JLabel numberLabel = new JLabel("Number: " + result.getString("number"));
                JLabel vehicleLabel = new JLabel("Vehicle: " + result.getString("vehicle"));
                JLabel licensePlateLabel = new JLabel("License Plate: " + result.getString("license_plate"));
                deliveryStatusLabel = new JLabel("Delivery Status: " + result.getString("delivery_status"));

                deliveryDetailsPanel.add(deliveryIdLabel);
                deliveryDetailsPanel.add(firstnameLabel);
                deliveryDetailsPanel.add(lastnameLabel);
                deliveryDetailsPanel.add(emailLabel);
                deliveryDetailsPanel.add(numberLabel);
                deliveryDetailsPanel.add(vehicleLabel);
                deliveryDetailsPanel.add(licensePlateLabel);
                deliveryDetailsPanel.add(deliveryStatusLabel);

                refreshDeliveryPanelVisibility(result.getString("delivery_status"));

            } else {
                JLabel noDeliveryLabel = new JLabel("No delivery details found.");
                deliveryDetailsPanel.add(noDeliveryLabel);
            }

            deliveryDetailsPanel.revalidate();
            deliveryDetailsPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshDeliveryPanelVisibility(String status) {
        // Show deliveries tab only if the delivery status is "Available"
        deliveriesTabbedPane.setVisible("Available".equals(status));
        if ("Available".equals(status)) {
            showPendingDeliveries();
            showActiveDeliveries();
        }
    }

    private void toggleDeliveryStatus() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT delivery_status FROM Delivery WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String currentStatus = result.getString("delivery_status");
                String newStatus = currentStatus.equals("Available") ? "Not Available" : "Available";

                String updateQuery = "UPDATE Delivery SET delivery_status = ? WHERE user_id = ?";
                PreparedStatement updateStatement = conn.prepareStatement(updateQuery);
                updateStatement.setString(1, newStatus);
                updateStatement.setInt(2, userId);
                updateStatement.executeUpdate();

                refreshDeliveryLabelStatus(newStatus);
                refreshDeliveryPanelVisibility(newStatus);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshDeliveryLabelStatus(String newStatus) {
        if (deliveryStatusLabel != null) {
            deliveryStatusLabel.setText("Delivery Status: " + newStatus);
            deliveryDetailsPanel.revalidate();
            deliveryDetailsPanel.repaint();
        }
    }

    private void showPendingDeliveries() {
        pendingDeliveriesPanel.removeAll();

        DefaultTableModel tableModel = new DefaultTableModel(
                new String[] { "Order ID", "Store", "Total Amount", "Delivery Address", "Status" }, 0);
        JTable table = new JTable(tableModel);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT o.order_id, s.name AS store_name, o.total_amount, CONCAT(u.address, ' ', u.address_number) AS delivery_address, o.status "
                    + "FROM Orders o "
                    + "JOIN Users u ON o.user_id = u.user_id "
                    + "JOIN Stores s ON o.store_id = s.store_id "
                    + "WHERE o.status = 'Completed'";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int orderId = result.getInt("order_id");
                String storeName = result.getString("store_name");
                double totalAmount = result.getDouble("total_amount");
                String deliveryAddress = result.getString("delivery_address");
                String deliveryStatus = result.getString("status");

                Object[] data = { orderId, storeName, totalAmount, deliveryAddress, deliveryStatus };
                tableModel.addRow(data);
            }

            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = table.rowAtPoint(evt.getPoint());
                    int orderId = (int) tableModel.getValueAt(row, 0);
                    new PendingDeliveryDetailsFrame(orderId);
                }
            });

            pendingDeliveriesPanel.add(new JScrollPane(table), BorderLayout.CENTER);
            pendingDeliveriesPanel.revalidate();
            pendingDeliveriesPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showActiveDeliveries() {
        activeDeliveriesPanel.removeAll();

        DefaultTableModel tableModel = new DefaultTableModel(
                new String[] { "Order ID", "Store", "Total Amount", "Delivery Address", "Status" }, 0);
        JTable table = new JTable(tableModel);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT o.order_id, s.name AS store_name, o.total_amount, CONCAT(u.address, ' ', u.address_number) AS delivery_address, o.status "
                    + "FROM Orders o "
                    + "JOIN Users u ON o.user_id = u.user_id "
                    + "JOIN Stores s ON o.store_id = s.store_id "
                    + "WHERE o.status = 'On the road'";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int orderId = result.getInt("order_id");
                String storeName = result.getString("store_name");
                double totalAmount = result.getDouble("total_amount");
                String deliveryAddress = result.getString("delivery_address");
                String deliveryStatus = result.getString("status");

                Object[] data = { orderId, storeName, totalAmount, deliveryAddress, deliveryStatus };
                tableModel.addRow(data);
            }

            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = table.rowAtPoint(evt.getPoint());
                    int orderId = (int) tableModel.getValueAt(row, 0);
                    new ActiveDeliveryDetailsFrame(orderId);
                }
            });

            activeDeliveriesPanel.add(new JScrollPane(table), BorderLayout.CENTER);
            activeDeliveriesPanel.revalidate();
            activeDeliveriesPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
