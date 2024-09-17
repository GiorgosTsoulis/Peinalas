import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

public class DeliveryRegisterFrame extends RegisterFrame {

    private JComboBox<String> vehicleComboBox;
    private JTextField licensePlateField;

    public DeliveryRegisterFrame(LoginFrame loginFrame) {
        super(loginFrame);

        // Vehicle Label
        JLabel vehicleLabel = new JLabel("Vehicle");
        gbc.gridx = 0;
        gbc.gridy = 9;
        this.add(vehicleLabel, gbc);

        // Vehicle ComboBox
        String[] vehicles = { "Car", "Bike", "Scooter" };
        vehicleComboBox = new JComboBox<>(vehicles);
        gbc.gridx = 1;
        gbc.gridy = 9;
        this.add(vehicleComboBox, gbc);

        // License Plate Label
        JLabel licensePlateLabel = new JLabel("License Plate");
        gbc.gridx = 0;
        gbc.gridy = 10;
        this.add(licensePlateLabel, gbc);

        // License Plate TextField
        licensePlateField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 10;
        this.add(licensePlateField, gbc);

        // Adjust Register Button position
        gbc.gridx = 1;
        gbc.gridy = 11;
        this.add(registerBtn, gbc);

        // Adjust Back Button position
        gbc.gridx = 2;
        gbc.gridy = 11;
        this.add(backBtn, gbc);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void registerUser() {
        String firstname = firstnameField.getText();
        String lastname = lastnameField.getText();
        String address = addressField.getText();
        String addressNumber = addressNumberField.getText();
        String postCode = postCodeField.getText();
        String country = (String) countryComboBox.getSelectedItem();
        String age = ageDatePicker.getDate() != null ? String.valueOf(ageDatePicker.getDate().getYear() + 1900) : "";
        String gender = (String) genderComboBox.getSelectedItem();
        String number = numberField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = "Delivery";
        String vehicle = (String) vehicleComboBox.getSelectedItem();
        String licensePlate = licensePlateField.getText();

        if (firstname.isEmpty() || lastname.isEmpty() || address.isEmpty() || addressNumber.isEmpty()
                || postCode.isEmpty() || country.isEmpty() || age.isEmpty() || gender.isEmpty() || number.isEmpty()
                || email.isEmpty() || username.isEmpty() || password.isEmpty() || licensePlate.isEmpty()) {
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
                            String query = "INSERT INTO DeliveryPendingRegistrations (username, password, role, firstname, lastname, address, address_number, post_code, country, age, phone_number, email, gender, vehicle, license_plate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement pst = conn.prepareStatement(query)) {
                                pst.setString(1, username);
                                pst.setString(2, password); // Consider hashing the password here
                                pst.setString(3, role);
                                pst.setString(4, firstname);
                                pst.setString(5, lastname);
                                pst.setString(6, address);
                                pst.setString(7, addressNumber);
                                pst.setString(8, postCode);
                                pst.setString(9, country);
                                pst.setInt(10, ageInt);
                                pst.setString(11, number);
                                pst.setString(12, email);
                                pst.setString(13, gender);
                                pst.setString(14, vehicle);
                                pst.setString(15, licensePlate);
                                pst.executeUpdate();
                            }

                            EmailSender.sendEmail(email, "Registration Pending", "Dear " + firstname
                                    + ",\n\nYour registration is pending approval. You will be notified once approved.");

                            JOptionPane.showMessageDialog(null, "Registration successful. Await approval.");
                            DeliveryRegisterFrame.this.dispose();
                            new LoginFrame().setVisible(true); // Open the login frame
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

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}