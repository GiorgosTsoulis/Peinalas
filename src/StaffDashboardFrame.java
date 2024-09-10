import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffDashboardFrame extends JFrame {

    private final int userId;
    private int storeId;
    private JPanel staffDetailsPanel;
    private JPanel buttonPanel;

    public StaffDashboardFrame(int userId) {
        super("Staff Dashboard");
        this.userId = userId;

        // Initialize the frame layout
        initializeLayout();

        // Load and display staff details
        displayStaffDetails();

        // Set the frame properties
        this.setPreferredSize(new Dimension(600, 500)); // Increased size for better layout
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeLayout() {
        // Set the layout manager for the frame
        setLayout(new BorderLayout());

        // Create and configure the staff details panel
        staffDetailsPanel = new JPanel();
        staffDetailsPanel.setLayout(new BoxLayout(staffDetailsPanel, BoxLayout.Y_AXIS));
        staffDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding
        add(staffDetailsPanel, BorderLayout.CENTER);

        // Create and configure the button panel
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding
        add(buttonPanel, BorderLayout.EAST);

        // Create buttons and add them to the button panel with spacing
        JButton menuManagerButton = new JButton("Menu Manager");
        menuManagerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMenuManager();
            }
        });
        buttonPanel.add(menuManagerButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add vertical space

        JButton orderManagerButton = new JButton("Order Manager");
        orderManagerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openOrderManager();
            }
        });
        buttonPanel.add(orderManagerButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add vertical space

        JButton couponManagerButton = new JButton("Coupon Management");
        couponManagerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCouponManager();
            }
        });
        buttonPanel.add(couponManagerButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginFrame();
            }
        });
        buttonPanel.add(logoutButton);
    }

    private void displayStaffDetails() {
        // Clear previous staff details
        staffDetailsPanel.removeAll();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error. Cannot load staff details.");
                return;
            }

            // Updated query to fetch store details
            String query = "SELECT u.firstname, u.lastname, u.email, u.number, s.position, s.staff_id, s.user_id, st.store_id, st.name AS store_name, st.location AS store_address, st.kitchenCategory, st.priceCategory, st.phone_number, st.website, st.opening_hours "
                    + "FROM Staff s "
                    + "JOIN Users u ON s.user_id = u.user_id "
                    + "JOIN Stores st ON s.store_id = st.store_id "
                    + "WHERE s.user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId); // Assuming userId is the ID of the staff whose details you want to fetch
            ResultSet rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                staffDetailsPanel.add(new JLabel("No staff found for User ID: " + userId));
            } else {
                while (rs.next()) {
                    int staffId = rs.getInt("staff_id");
                    int userId = rs.getInt("user_id");
                    String position = rs.getString("position");
                    String firstname = rs.getString("firstname");
                    String lastname = rs.getString("lastname");
                    String email = rs.getString("email");
                    String phoneNumber = rs.getString("number");
                    storeId = rs.getInt("store_id");
                    String storeName = rs.getString("store_name");
                    String storeAddress = rs.getString("store_address");
                    String kitchenCategory = rs.getString("kitchenCategory");
                    String priceCategory = rs.getString("priceCategory");
                    String storePhoneNumber = rs.getString("phone_number");
                    String storeWebsite = rs.getString("website");
                    String storeOpeningHours = rs.getString("opening_hours");

                    // Add staff details to the panel as labels
                    staffDetailsPanel.add(new JLabel("Staff ID: " + staffId));
                    staffDetailsPanel.add(new JLabel("User ID: " + userId));
                    staffDetailsPanel.add(new JLabel("Name: " + firstname + " " + lastname));
                    staffDetailsPanel.add(new JLabel("Email: " + email));
                    staffDetailsPanel.add(new JLabel("Phone: " + phoneNumber));
                    staffDetailsPanel.add(new JLabel("Position: " + position));
                    staffDetailsPanel.add(new JLabel("-----------------------------")); // Separator

                    // Add store details below the separator
                    staffDetailsPanel.add(new JLabel("Store ID: " + storeId));
                    staffDetailsPanel.add(new JLabel("Store Name: " + storeName));
                    staffDetailsPanel.add(new JLabel("Store Address: " + storeAddress));
                    staffDetailsPanel.add(new JLabel("Kitchen Category: " + kitchenCategory));
                    staffDetailsPanel.add(new JLabel("Price Category: " + priceCategory));
                    staffDetailsPanel.add(new JLabel("Store Phone Number: " + storePhoneNumber));
                    staffDetailsPanel.add(new JLabel("Store Website: " + storeWebsite));
                    staffDetailsPanel.add(new JLabel("Opening Hours: " + storeOpeningHours));
                }
            }

            // Refresh the staff details panel to show updated content
            staffDetailsPanel.revalidate();
            staffDetailsPanel.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMenuManager() {
        // Create and open the MenuManager frame
        new MenuManager(storeId);
    }

    private void openOrderManager() {
        // Create and open the OrderManager frame
        new OrderManager(storeId);
    }

    private void openCouponManager() {
        // Create and open the CouponManager frame
        new CouponManager(userId);
    }
}
