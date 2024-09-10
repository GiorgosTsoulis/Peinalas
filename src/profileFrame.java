import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class profileFrame extends JFrame {

    private JLabel userNameLabel;
    private JLabel userEmailLabel;
    private JLabel userPhoneLabel;
    private JLabel userAddressLabel;

    private JButton editProfileBtn;
    private JButton previousOrdersBtn;
    private JButton backBtn;

    private int userId; // Store user ID

    public profileFrame(int userId) {
        this.userId = userId;

        setTitle("My Profile");
        setLayout(new BorderLayout());

        // Initialize components
        userNameLabel = new JLabel();
        userEmailLabel = new JLabel();
        userPhoneLabel = new JLabel();
        userAddressLabel = new JLabel();

        editProfileBtn = new JButton("Edit Profile");
        previousOrdersBtn = new JButton("Previous Orders");
        backBtn = new JButton("Back");

        // Panel for profile details
        JPanel profileDetailsPanel = new JPanel(new GridLayout(4, 1));
        profileDetailsPanel.add(userNameLabel);
        profileDetailsPanel.add(userEmailLabel);
        profileDetailsPanel.add(userPhoneLabel);
        profileDetailsPanel.add(userAddressLabel);

        // Add profile details panel to the top of the frame
        add(profileDetailsPanel, BorderLayout.NORTH);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editProfileBtn);
        buttonPanel.add(previousOrdersBtn);
        buttonPanel.add(backBtn);

        // Add button panel to the bottom of the frame
        add(buttonPanel, BorderLayout.SOUTH);

        // Load user details
        loadUserProfile();

        // Add action listeners
        editProfileBtn.addActionListener(e -> new EditProfileFrame(userId, this));
        previousOrdersBtn.addActionListener(e -> new PreviousOrdersFrame(userId));
        backBtn.addActionListener(e -> this.dispose());

        setPreferredSize(new Dimension(600, 500));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadUserProfile() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            String query = "SELECT firstname, lastname, email, number, address FROM Users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("firstname") + " " + rs.getString("lastname");
                userNameLabel.setText("Name: " + fullName);
                userEmailLabel.setText("Email: " + rs.getString("email"));
                userPhoneLabel.setText("Phone: " + rs.getString("number"));
                userAddressLabel.setText("Address: " + rs.getString("address"));
            } else {
                JOptionPane.showMessageDialog(this, "User profile not found for user_id: " + userId);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
        }
    }

    // Method to update profile details after editing
    public void updateProfileDetails() {
        loadUserProfile(); // Reload the updated profile details
    }
}
