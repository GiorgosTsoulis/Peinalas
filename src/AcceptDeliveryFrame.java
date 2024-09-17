import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AcceptDeliveryFrame extends JFrame {

    private JTable pendingDeliveriesTable;
    private JButton acceptButton, rejectButton, backButton;
    private DefaultTableModel tableModel;

    public AcceptDeliveryFrame() {
        this.setTitle("Accept/Reject Delivery Registrations");

        initializeLayout();

        this.setPreferredSize(new Dimension(1200, 600));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeLayout() {
        this.setLayout(new BorderLayout());

        // Create table model with columns
        String[] columnNames = { "Pending ID", "Username", "Password", "Role", "First Name", "Last Name", "Address",
                "Address Number", "Post Code", "Country", "Age", "Phone Number", "Email", "Gender", "Vehicle",
                "License Plate", "Status" };
        tableModel = new DefaultTableModel(columnNames, 0);
        pendingDeliveriesTable = new JTable(tableModel);

        loadPendingDeliveries();

        JScrollPane scrollPane = new JScrollPane(pendingDeliveriesTable);
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        acceptButton = new JButton("Accept");
        rejectButton = new JButton("Reject");
        backButton = new JButton("Back");

        acceptButton.addActionListener(e -> updateDeliveryStatus("accepted"));
        rejectButton.addActionListener(e -> updateDeliveryStatus("rejected"));
        backButton.addActionListener(e -> this.dispose());

        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(backButton);

        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    // Load pending delivery registrations from DeliveryPendingRegistrations table
    private void loadPendingDeliveries() {
        // Clear the table before loading new data
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            String query = "SELECT * FROM DeliveryPendingRegistrations WHERE status = 'pending'";
            try (PreparedStatement pst = conn.prepareStatement(query);
                    ResultSet rs = pst.executeQuery()) {

                // Load each row from the result set into the table model
                while (rs.next()) {
                    Object[] rowData = new Object[] {
                            rs.getInt("pending_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("address"),
                            rs.getString("address_number"),
                            rs.getString("post_code"),
                            rs.getString("country"),
                            rs.getInt("age"),
                            rs.getString("phone_number"),
                            rs.getString("email"),
                            rs.getString("gender"),
                            rs.getString("vehicle"),
                            rs.getString("license_plate"),
                            rs.getString("status")
                    };
                    tableModel.addRow(rowData);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading pending deliveries: " + ex.getMessage());
        }
    }

    // Update the status of the selected delivery registration
    private void updateDeliveryStatus(String status) {
        int selectedRow = pendingDeliveriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a delivery registration to " + status + ".");
            return;
        }

        // Make sure the row index is valid
        if (selectedRow >= tableModel.getRowCount() || selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selected row is out of bounds.");
            return;
        }

        int pendingId = (Integer) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            // Check if the delivery registration exists
            String checkQuery = "SELECT pending_id FROM DeliveryPendingRegistrations WHERE pending_id = ? AND status = 'pending'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, pendingId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Update the delivery registration status
                    String updateQuery = "UPDATE DeliveryPendingRegistrations SET status = ? WHERE pending_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, status);
                        updateStmt.setInt(2, pendingId);
                        int rowsAffected = updateStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            if (status.equals("accepted")) {
                                // Insert into Users table
                                String insertUserQuery = "INSERT INTO Users (username, password, role, firstname, lastname, address, address_number, post_code, country, age, number, email, gender) "
                                        + "SELECT username, password, role, firstname, lastname, address, address_number, post_code, country, age, phone_number, email, gender "
                                        + "FROM DeliveryPendingRegistrations WHERE pending_id = ?";
                                try (PreparedStatement insertUserStmt = conn.prepareStatement(insertUserQuery)) {
                                    insertUserStmt.setInt(1, pendingId);
                                    insertUserStmt.executeUpdate();
                                }

                                // Insert into Delivery table
                                String insertDeliveryQuery = "INSERT INTO Delivery (user_id, vehicle, license_plate) "
                                        + "SELECT user_id, vehicle, license_plate FROM Users JOIN DeliveryPendingRegistrations "
                                        + "ON Users.username = DeliveryPendingRegistrations.username WHERE pending_id = ?";
                                try (PreparedStatement insertDeliveryStmt = conn
                                        .prepareStatement(insertDeliveryQuery)) {
                                    insertDeliveryStmt.setInt(1, pendingId);
                                    insertDeliveryStmt.executeUpdate();

                                }

                                // Send notification email
                                String email = (String) tableModel.getValueAt(selectedRow, 12);
                                String firstname = (String) tableModel.getValueAt(selectedRow, 4);
                                EmailSender.sendEmail(email, "Registration " + status, "Dear " + firstname
                                        + ",\n\nYour registration has been " + status
                                        + ". You can now login to the system.");
                            } else {
                                // Send notification email
                                String email = (String) tableModel.getValueAt(selectedRow, 12);
                                String firstname = (String) tableModel.getValueAt(selectedRow, 4);
                                EmailSender.sendEmail(email, "Registration " + status, "Dear " + firstname
                                        + ",\n\nYour registration has been " + status
                                        + ". Please contact support for more information.");
                            }

                            JOptionPane.showMessageDialog(this, "Delivery registration " + status + " successfully.");

                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to change the status. Please try again.");
                        }
                        // Reload the table data
                        loadPendingDeliveries();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Pending ID not found or already processed.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating delivery registration status: " + e.getMessage());
        } catch (ClassCastException e) {
            JOptionPane.showMessageDialog(this, "Error casting table model values: " + e.getMessage());
        }
    }

}
