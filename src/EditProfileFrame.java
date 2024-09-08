import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class EditProfileFrame extends JFrame {

    private JTextField firstnameField;
    private JTextField lastnameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton saveBtn;

    private int userId;
    private profileFrame myProfileFrame;

    public EditProfileFrame(int userId, profileFrame myProfileFrame) {
        this.userId = userId;
        this.myProfileFrame = myProfileFrame;

        setTitle("Edit Profile");
        setLayout(new GridLayout(8, 2)); // Adjust layout to accommodate password fields

        // Initialize components
        firstnameField = new JTextField();
        lastnameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        addressField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        saveBtn = new JButton("Save");

        // Add components to the frame
        add(new JLabel("First Name:"));
        add(firstnameField);
        add(new JLabel("Last Name:"));
        add(lastnameField);
        add(new JLabel("Email:"));
        add(emailField);
        add(new JLabel("Phone:"));
        add(phoneField);
        add(new JLabel("Address:"));
        add(addressField);
        add(new JLabel("New Password:"));
        add(passwordField);
        add(new JLabel("Confirm Password:"));
        add(confirmPasswordField);
        add(saveBtn);

        // Load current user details
        loadUserProfile();

        // Add action listener to the save button
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUserProfile();
            }
        });

        setPreferredSize(new Dimension(300, 300)); // Adjust size to accommodate all fields
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
                firstnameField.setText(rs.getString("firstname"));
                lastnameField.setText(rs.getString("lastname"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("number"));
                addressField.setText(rs.getString("address"));
            } else {
                JOptionPane.showMessageDialog(this, "User profile not found.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
        }
    }

    private void updateUserProfile() {
        String firstname = firstnameField.getText();
        String lastname = lastnameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validate input
        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.");
            return;
        }

        // Validate passwords
        if (!password.isEmpty() || !confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.");
                return;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            // Prepare the SQL query
            String query = "UPDATE Users SET firstname = ?, lastname = ?, email = ?, number = ?, address = ?"
                    + (password.isEmpty() ? "" : ", password = ?") + " WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, firstname);
            pstmt.setString(2, lastname);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, address);

            if (!password.isEmpty()) {
                pstmt.setString(6, password); // Store the plain-text password
                pstmt.setInt(7, userId);
            } else {
                pstmt.setInt(6, userId);
            }

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully.");

                if (myProfileFrame != null) {
                    myProfileFrame.updateProfileDetails(); // Refresh profile details
                }

                dispose(); // Close the EditProfile frame
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage());
        }
    }
}
