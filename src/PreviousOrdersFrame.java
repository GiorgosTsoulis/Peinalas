import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class PreviousOrdersFrame extends JFrame {

    private JTable ordersTable;
    private JScrollPane scrollPane;
    private int userId;

    public PreviousOrdersFrame(int userId) {
        this.userId = userId; // Store the userId
        setTitle("Previous Orders");
        setLayout(new BorderLayout());

        // Initialize components
        ordersTable = new JTable();
        scrollPane = new JScrollPane(ordersTable);

        // Add table to scroll pane and set layout
        add(scrollPane, BorderLayout.CENTER);

        // Load previous orders for the given user ID
        loadPreviousOrders();

        setPreferredSize(new Dimension(800, 600));
        pack();
        this.setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadPreviousOrders() {
        // SQL query to get orders for the specified user ID
        String query = "SELECT order_id, order_date, status, total_amount, service_type "
                + "FROM Orders "
                + "WHERE user_id = ? AND status = 'Completed'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                // Create table model with column names
                String[] columnNames = { "Order ID", "Order Date", "Status", "Total Amount", "Service Type" };
                DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    Timestamp orderDate = rs.getTimestamp("order_date");
                    String status = rs.getString("status");
                    double totalAmount = rs.getDouble("total_amount");
                    String serviceType = rs.getString("service_type");

                    // Format date and amount for display
                    String formattedDate = (orderDate != null)
                            ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate)
                            : "";
                    String formattedAmount = String.format("%.2f", totalAmount);

                    Object[] row = { orderId, formattedDate, status, formattedAmount, serviceType };
                    tableModel.addRow(row);
                }

                ordersTable.setModel(tableModel);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading previous orders: " + ex.getMessage());
        }
    }
}
