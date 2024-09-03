import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class CustomerRegisterFrame extends RegisterFrame {

    public CustomerRegisterFrame(LoginFrame loginFrame) {
        super(loginFrame);

        // Adjust Register Button position
        gbc.gridx = 1;
        gbc.gridy = 8;
        this.add(registerBtn, gbc);

        // Adjust Back Button position
        gbc.gridx = 2;
        gbc.gridy = 8;
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
        String role = "Customer";

        if (firstname.isEmpty() || lastname.isEmpty() || address.isEmpty() || addressNumber.isEmpty()
                || postCode.isEmpty() || country.isEmpty() || age.isEmpty() || gender.isEmpty() || number.isEmpty()
                || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
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
                            String query = "INSERT INTO Users (firstname, lastname, address, address_number, post_code, country, age, gender, number, email, username, password, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement pst = conn.prepareStatement(query)) {
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
                                pst.setString(13, role); // Set role in the database
                                pst.executeUpdate();

                                // Send successful registration email
                                EmailSender.sendEmail(email, "Registration Successful", "Dear " + firstname
                                        + ",\n\nYour registration was successful. Welcome to our platform!");

                                JOptionPane.showMessageDialog(null, "Registration successful. Please log in.");
                                CustomerRegisterFrame.this.dispose();
                                new LoginFrame().setVisible(true); // Open the login frame
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

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
