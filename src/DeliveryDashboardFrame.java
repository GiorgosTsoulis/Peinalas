import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;

public class DeliveryDashboardFrame extends DashboardFrame {

    private JPanel deliveryDetailsPanel, buttonPanel;
    private JToggleButton deliveryStatusButton;
    private JButton logoutButton;
    private JLabel deliveryStatusLabel;

    private int userId;

    public DeliveryDashboardFrame(int userId) {
        super("Delivery");
        this.userId = userId;

        initializeLayout();

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeLayout() {
        setLayout(new BorderLayout());

        deliveryDetailsPanel = new JPanel();
        deliveryDetailsPanel.setLayout(new BoxLayout(deliveryDetailsPanel, BoxLayout.Y_AXIS));
        deliveryDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(deliveryDetailsPanel, BorderLayout.CENTER);

        displayDeliveryDetails();

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buttonPanel, BorderLayout.EAST);

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

        // Add buttons with padding
        buttonPanel.add(deliveryStatusButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Adds 15px vertical padding between buttons
        buttonPanel.add(logoutButton);

        deliveryDetailsPanel.add(buttonPanel);

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

                JOptionPane.showMessageDialog(this, "Delivery status updated to: " + newStatus);
                refreshDeliveryLabelStatus(newStatus);
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

}
