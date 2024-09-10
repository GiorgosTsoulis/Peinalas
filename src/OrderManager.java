import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class OrderManager extends JFrame {

    private int storeId;
    private JPanel ordersPanel;
    private JScrollPane scrollPanel;
    private DefaultTableModel pendingTableModel, inProgressTableModel, cancelledTableModel, completedTableModel;
    private JTable pendingTable, inProgressTable, cancelledTable, completedTable;

    public OrderManager(int storeId) {
        this.setTitle("Order Manager");
        this.storeId = storeId;

        // Initialize the frame layout
        initializeLayout();

        this.setPreferredSize(new Dimension(800, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeLayout() {
        setLayout(new BorderLayout());

        ordersPanel = new JPanel();
        ordersPanel.setLayout(new GridBagLayout());
        scrollPanel = new JScrollPane(ordersPanel);

        // Create tabbed pane for different statuses
        JTabbedPane tabbedPane = new JTabbedPane();

        // Initialize tables for each status
        pendingTableModel = new DefaultTableModel(
                new String[] { "Order ID", "User ID", "Order Date", "Total Amount", "Service Type", "Status" }, 0);
        pendingTable = new JTable(pendingTableModel);

        inProgressTableModel = new DefaultTableModel(
                new String[] { "Order ID", "User ID", "Order Date", "Total Amount", "Service Type", "Status" }, 0);
        inProgressTable = new JTable(inProgressTableModel);

        cancelledTableModel = new DefaultTableModel(
                new String[] { "Order ID", "User ID", "Order Date", "Total Amount", "Service Type", "Status" }, 0);
        cancelledTable = new JTable(cancelledTableModel);

        completedTableModel = new DefaultTableModel(
                new String[] { "Order ID", "User ID", "Order Date", "Total Amount", "Service Type", "Status" }, 0);
        completedTable = new JTable(completedTableModel);

        // Add tables to scroll panes
        tabbedPane.addTab("Pending Orders", new JScrollPane(pendingTable));
        tabbedPane.addTab("In Progress Orders", new JScrollPane(inProgressTable));
        tabbedPane.addTab("Cancelled Orders", new JScrollPane(cancelledTable));
        tabbedPane.addTab("Completed Orders", new JScrollPane(completedTable));

        // Load the orders into the tables
        populateOrdersTable("Pending", pendingTableModel);
        populateOrdersTable("In Progress", inProgressTableModel);
        populateOrdersTable("Cancelled", cancelledTableModel);
        populateOrdersTable("Completed", completedTableModel);

        // Load and display active orders
        loadOrders();

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

        pendingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = pendingTable.getSelectedRow();
                if (selectedRow != -1) {
                    int orderId = (int) pendingTableModel.getValueAt(selectedRow, 0);
                    new OrderDetails(orderId, OrderManager.this); // Open OrderDetails frame
                }
            }
        });

        cancelledTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = cancelledTable.getSelectedRow();
                if (selectedRow != -1) {
                    int orderId = (int) cancelledTableModel.getValueAt(selectedRow, 0);
                    new OrderDetails(orderId, null); // Open OrderDetails frame
                }
            }
        });

        completedTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = completedTable.getSelectedRow();
                if (selectedRow != -1) {
                    int orderId = (int) completedTable.getValueAt(selectedRow, 0);
                    new OrderDetails(orderId, null); // Open OrderDetails frame
                }
            }
        });

        inProgressTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = inProgressTable.getSelectedRow();
                if (selectedRow != -1) {
                    int orderId = (int) inProgressTable.getValueAt(selectedRow, 0);
                    new OrderDetails(orderId, OrderManager.this); // Open OrderDetails frame
                }
            }
        });

        add(scrollPanel, BorderLayout.CENTER);
        add(tabbedPane, BorderLayout.WEST);
        add(backBtnPanel, BorderLayout.SOUTH);
    }

    private void populateOrdersTable(String status, DefaultTableModel tableModel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            // Query based on status
            String query = "SELECT order_id, user_id, order_date, total_amount, service_type, status "
                    + "FROM Orders WHERE store_id = ? AND status = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, storeId);
            pstmt.setString(2, status);
            ResultSet rs = pstmt.executeQuery();

            // Clear previous rows
            tableModel.setRowCount(0);

            // Add rows to the table model
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int userId = rs.getInt("user_id");
                String orderDate = rs.getString("order_date");
                double totalAmount = rs.getDouble("total_amount");
                String serviceType = rs.getString("service_type");
                String orderStatus = rs.getString("status");

                tableModel.addRow(new Object[] { orderId, userId, orderDate, totalAmount, serviceType, orderStatus });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                    + "WHERE o.store_id = ? AND o.status = 'Pending'";

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

                orderGbc.gridy = 1;
                orderGbc.gridwidth = 1;
                orderGbc.gridx = 0;
                orderPanel.add(confirmButton, orderGbc);

                orderGbc.gridx = 1;
                orderPanel.add(cancelButton, orderGbc);

                gbc.gridy++;
                gbc.gridx = 0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                ordersPanel.add(orderPanel, gbc);

                // Button action listeners to update order status
                confirmButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateOrderStatus(orderId, "In Progress");

                    }
                });

                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateOrderStatus(orderId, "Cancelled");

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

    public void updateOrderStatus(int orderId, String status) {
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
                    OrderNotificationScheduler.checkOrdersAndSendNotifications(orderId);
                    JOptionPane.showMessageDialog(this, "Status changed successfully and email sent to client.");
                    refreshTables();

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

    public void refreshTables() {
        populateOrdersTable("Pending", pendingTableModel);
        populateOrdersTable("In Progress", inProgressTableModel);
        populateOrdersTable("Cancelled", cancelledTableModel);
        populateOrdersTable("Completed", completedTableModel);
    }
}