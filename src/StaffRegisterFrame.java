import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class StaffRegisterFrame extends RegisterFrame {

    private JComboBox<String> storeComboBox;
    private JComboBox<String> positionComboBox;

    private static final String[] JOB_POSITIONS = { "Manager", "Chef", "Waiter", "Cleaner" };

    public StaffRegisterFrame(LoginFrame loginFrame) {
        super(loginFrame);

        // Store Label
        JLabel storeLabel = new JLabel("Store");
        gbc.gridx = 0;
        gbc.gridy = 8;
        this.add(storeLabel, gbc);

        // Store ComboBox
        storeComboBox = new JComboBox<>(getStores());
        gbc.gridx = 1;
        gbc.gridy = 8;
        this.add(storeComboBox, gbc);

        // Position Label
        JLabel positionLabel = new JLabel("Position");
        gbc.gridx = 0;
        gbc.gridy = 9;
        this.add(positionLabel, gbc);

        // Position ComboBox
        positionComboBox = new JComboBox<>(JOB_POSITIONS);
        gbc.gridx = 1;
        gbc.gridy = 9;
        this.add(positionComboBox, gbc);

        // Adjust Register Button position
        gbc.gridx = 1;
        gbc.gridy = 10;
        this.add(registerBtn, gbc);

        // Adjust Back Button position
        gbc.gridx = 2;
        gbc.gridy = 10;
        this.add(backBtn, gbc);
    }

    @Override
    protected void registerUser() {
        String firstname = firstnameField.getText();
        String lastname = lastnameField.getText();
        String address = addressField.getText();
        String addressNumber = addressNumberField.getText();
        String postCode = postCodeField.getText();
        String country = (String) countryComboBox.getSelectedItem();
        @SuppressWarnings("deprecation")
        String age = ageDatePicker.getDate() != null ? String.valueOf(ageDatePicker.getDate().getYear() + 1900) : "";
        String gender = (String) genderComboBox.getSelectedItem();
        String number = numberField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = "Staff";
        String store = (String) storeComboBox.getSelectedItem();
        String position = (String) positionComboBox.getSelectedItem();

        if (firstname.isEmpty() || lastname.isEmpty() || address.isEmpty() || addressNumber.isEmpty()
                || postCode.isEmpty() || country.isEmpty() || age.isEmpty() || gender.isEmpty() || number.isEmpty()
                || email.isEmpty() || username.isEmpty() || password.isEmpty() || position.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields must be filled out.");
            return;
        }

        if (!Pattern.matches("^[a-z0-9]+$", username)) {
            JOptionPane.showMessageDialog(null, "Username must contain only lowercase letters and numbers.");
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(null, "Password must be at least 8 characters long.");
            return;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            JOptionPane.showMessageDialog(null, "Invalid email format.");
            return;
        }

        // Generate verification code
        String verificationCode = generateVerificationCode();
        EmailSender.sendEmail(email, "Email Verification", "Your verification code is: " + verificationCode);

        // Open email verification frame
        new EmailVerificationFrame(email, verificationCode, firstname + " " + lastname,
                new EmailVerificationCallback() {
                    @Override
                    public void onVerificationSuccess() {
                        // Insert user into database after successful verification
                        try (Connection conn = DatabaseConnection.getConnection()) {
                            // Check if username already exists
                            String checkUsernameQuery = "SELECT COUNT(*) FROM Users WHERE username = ?";
                            try (PreparedStatement checkUsernameStmt = conn.prepareStatement(checkUsernameQuery)) {
                                checkUsernameStmt.setString(1, username);
                                try (ResultSet rs = checkUsernameStmt.executeQuery()) {
                                    if (rs.next() && rs.getInt(1) > 0) {
                                        JOptionPane.showMessageDialog(null, "Username already exists.");
                                        return;
                                    }
                                }
                            }

                            // Check if email already exists
                            String checkEmailQuery = "SELECT COUNT(*) FROM Users WHERE email = ?";
                            try (PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailQuery)) {
                                checkEmailStmt.setString(1, email);
                                try (ResultSet rs = checkEmailStmt.executeQuery()) {
                                    if (rs.next() && rs.getInt(1) > 0) {
                                        JOptionPane.showMessageDialog(null, "Email already exists.");
                                        return;
                                    }
                                }
                            }

                            int ageInt = Integer.parseInt(age);
                            String query = "INSERT INTO Users (firstname, lastname, address, address_number, post_code, country, age, gender, number, email, username, password, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement pst = conn.prepareStatement(query,
                                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                                pst.setString(1, firstname);
                                pst.setString(2, lastname);
                                pst.setString(3, address);
                                pst.setString(4, addressNumber);
                                pst.setString(5, postCode);
                                pst.setString(6, country);
                                pst.setInt(7, ageInt);
                                pst.setString(8, gender);
                                pst.setString(9, number);
                                pst.setString(10, email);
                                pst.setString(11, username);
                                pst.setString(12, password);
                                pst.setString(13, role);
                                pst.executeUpdate();

                                // Get the generated user_id
                                int userId;
                                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                                    if (generatedKeys.next()) {
                                        userId = generatedKeys.getInt(1);
                                    } else {
                                        throw new SQLException("Creating user failed, no ID obtained.");
                                    }
                                }

                                // Get store ID
                                int storeId = getStoreId(store);
                                if (storeId == 0) {
                                    JOptionPane.showMessageDialog(null, "Store not found.");
                                    return;
                                }

                                // Insert into Staff table
                                String staffQuery = "INSERT INTO Staff (user_id, store_id, position) VALUES (?, ?, ?)";
                                try (PreparedStatement staffPst = conn.prepareStatement(staffQuery)) {
                                    staffPst.setInt(1, userId);
                                    staffPst.setInt(2, storeId);
                                    staffPst.setString(3, position);
                                    staffPst.executeUpdate();
                                }

                                JOptionPane.showMessageDialog(null, "Registration successful.");
                                StaffRegisterFrame.this.dispose();
                            }
                        } catch (SQLException ex) {
                            if (ex instanceof java.sql.SQLNonTransientConnectionException) {
                                JOptionPane.showMessageDialog(null, "Database connection error.");
                            } else {
                                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                            }
                            clearFields();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Age must be a valid number.");
                            ageDatePicker.setDate(null);
                        }
                    }

                    @Override
                    public void onVerificationFailure() {
                        JOptionPane.showMessageDialog(null, "Email verification failed. Registration not completed.");
                    }
                });
    }

    private String[] getStores() {
        List<String> stores = new ArrayList<>();
        String query = "SELECT name FROM Stores";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                stores.add(rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return stores.toArray(new String[0]);
    }

    private int getStoreId(String store) {
        int storeId = 0;
        String query = "SELECT store_id FROM Stores WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, store);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    storeId = rs.getInt("store_id");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return storeId;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
